package com.dlink.dview8.probe;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dlink.dview8.probe.discovery.ProbeDiscoveryMgrProvider;
import com.dlink.dview8.probe.discovery.model.DiscoveryTask;
import com.dlink.dview8.probe.executors.ThreadPoolSwitchExecutors;
import com.dlink.dview8.probe.north.io.SocketKeepAlivedTask;
import com.dlink.dview8.probe.north.io.WebSocketProvider;
import com.dlink.dview8.probe.protocol.Snmp4jProvider;
import com.dlink.dview8.probe.protocol.SnmpService;
import com.dlink.dview8.probe.service.ProbeApiProvider;
import com.dlink.dview8.common.api.Dview8Service;
import com.dlink.dview8.probe.api.task.ProbeApiTask;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> Probe生命周期的管理，Probe内部服务：启动、停止、注册、注销，并提供线程池服务；
 *  
 * @author Sun Tian Yu
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 */
public final class ProbeProvider {

    /**
     * 日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeProvider.class);
    /**
     * Probe任务获取线程池
     */
    private ThreadPoolExecutor acquireApiTaskExecutor;
    /**
     * Probe服务线程池
     */
    private ThreadPoolExecutor probeApiTaskExecutor;
    
    /**
     *  单个设备操作线程池 
     */
    private ThreadPoolExecutor secondPriTaskExecutor;
    
    /**
     *  查询操作、Probe级别配置任务线程池 
     */
    private ThreadPoolExecutor priorityTaskExecutor;
    
    /**
     *  返回操作结果、采样信息任务线程池 
     */
    private ThreadPoolExecutor outputTaskExecutor;
    
    /**
     *  批量设备操作线程池 
     */
    private ThreadPoolExecutor batchDevicesExecutor;
    
    /**
     *  批量设备操作线程池 
     */
    private ThreadPoolSwitchExecutors<String> batchDeviceExecutors;
    
    /**
     *  Snmp服务 
     */
    private static SnmpService snmpService;
    
    /**
     *  对整个Probe进行生命周期管理 
     */
    private static volatile ProbeProvider probeProvider;
    
    /** 
     * 阻塞队列容量 
     */
    private static final int MAX_QUEUE_SIZE = 8000;
    
    /**
     *  批量设备操作，线程池最大容量 
     */
    private static final int MAX_DEVICE_THREAD = 200;
    
    /**
     * 批量设备操作，线程池最小容量
     */
    private static final int MIN_DEVICE_THREAD = 50;
    
    /**
     * Probe API线程池容量
     */
    private static final int PROBE_API_THREAD = 10;
    
    /**
     * Probe服务
     */
    private static final Dview8Service probeService = new ProbeApiProvider();
    
    /**
     * 连接CoreServer
     */
    private static final String url = "ws://localhost:8195/websocket";
    
    /* 单例模式 */
    private ProbeProvider() {
        
    }
    
    /**
     * 
     * Description: 双检锁 单例模式
     *  
     * @author XXX<br>
     * @return <br>
     */
    public static ProbeProvider createInstance() {
        if (probeProvider == null) {
            synchronized (ProbeProvider.class) {
                if (probeProvider == null) {
                    probeProvider = new ProbeProvider();
                }
            }
        }
        return probeProvider;
    }
    
    /**
     * Probe Service Start.
     * 
     * @param
     * @return
     */
    public void start() {
        LOG.info("ProbeProvider Starting");
        
        /* WebSocket初始化 */
        probeIoStart();
        
        /* Probe线程池初始化 */
        batchDevicesTaskStart();
        probeApiTaskStart();
        priorityTaskStart();
        secondPriTaskStart();
        outputTaskStart();
        
        /* Probe Protocol开启 */
        protocolStart();
         
        /* Probe管理类初始化 */
        mgrStart();
        
        /* 开始获取CoreServer下发的任务 */
        acquireApiTaskStart();
        
        LOG.info("ProbeProvider Started");
    }

    /**
     * 
     * Description: Probe Service Close.
     *  
     * @author XXX<br>
     * @throws Exception <br>
     */
    public void close() throws Exception {
        LOG.info("ProbeProvider Closed -{}", Utils.getLineInfo());
        
        /* 获取任务线程池关闭 */
        if (acquireApiTaskExecutor != null) {
            acquireApiTaskExecutor.shutdown();
            acquireApiTaskExecutor = null;
        }
        
        /* ProbeIo服务关闭 */
        probeIoClose();
        
        /* Probe管理类关闭 */
        mgrStop();
        
        /* Probe Protocol关闭 */
        protocolClose();
        
        /* Probe线程池关闭 */
        if (outputTaskExecutor != null) {
            outputTaskExecutor.shutdown();
            outputTaskExecutor = null;
        }
        if (secondPriTaskExecutor != null) {
            secondPriTaskExecutor.shutdown();
            secondPriTaskExecutor = null;
        }
        if (priorityTaskExecutor != null) {
            priorityTaskExecutor.shutdown();
            priorityTaskExecutor = null;
        }
        if (probeApiTaskExecutor != null) {
            probeApiTaskExecutor.shutdown();
            probeApiTaskExecutor = null;
        }
        if (batchDevicesExecutor != null) {
            batchDevicesExecutor.shutdown();
            batchDevicesExecutor = null;
        }
    }
    
    /**
     * Probe All Managers Start.
     *   <p>include Discovery-Manager.
     *   <p>include Monitor-Managers
     *   <p>include File-Managers
     * 
     * @param
     * @return
     */
    private void mgrStart() {
        ProbeManagers.create(DiscoveryTask.class, ProbeDiscoveryMgrProvider.class);
    }

    /**
     * Probe All Managers Close.
     *   <p>include Discovery-Manager.
     *   <p>include Monitor-Managers
     *   <p>include File-Managers
     * 
     * @param
     * @return
     */
    private void mgrStop() {
        ProbeManagers.clear();
    }
    
    /**
     * 
     * Description: ProbeIO服务初始化
     *  
     * @author XXX<br> <br>
     * @throws InterruptedException 
     */
    private void probeIoStart() {
   
        /* 连接CoreServer */
        try {
            SocketKeepAlivedTask keepAlived = new SocketKeepAlivedTask(url);  
            Thread thread = new Thread(keepAlived);
            thread.setName("keep-alived-T");
            thread.setDaemon(true);
            thread.start();
            
            while(SocketKeepAlivedTask.getSocketProvider() == null ||
                    !SocketKeepAlivedTask.getSocketProvider().isProbeSocketReady()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // TODO 异常处理
            
        }
        LOG.info("ProbeIO Start Success.");
    }
    
    /**
     * 
     * Description: ProbeIO服务关闭
     *  
     * @author XXX<br> <br>
     */
    private void probeIoClose() {
        SocketKeepAlivedTask.setClosed(true);
    }
    
    /**
     * Probe Protocol Start
     *   <p>include SNMP
     *   <p>include SSH
     *   <p>include TELNET
     *   <p>include TFTP
     * 
     * @param
     * @return
     */
    private void protocolStart() {
        snmpService = new Snmp4jProvider();
    }
    
    /**
     * Probe Protocol Close
     *   <p>include SNMP.
     *   <p>include SSH
     *   <p>include TELNET
     *   <p>include TFTP
     * 
     * @param
     * @return
     */
    private void protocolClose() {
        snmpService = null;
    }
    
    /**
     * Submit Task to Probe.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void acquireApiTaskSubmit(Runnable task) {
        if (probeProvider.acquireApiTaskExecutor == null) {
            return;
        }
        try {
            probeProvider.acquireApiTaskExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            LOG.error("probe service queue is full");
        }
    }
    
    /**
     * Submit Task to probeApi-threadPool.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void probeApiTaskSubmit(Runnable task) {
        if (probeProvider.probeApiTaskExecutor == null) {
            return;
        }
        try {
            probeProvider.probeApiTaskExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.queueFullExceptionOutput();
        }
    }
    
    /**
     * Submit task to dataQuery-threadPool.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void priApiTaskSubmit(Runnable task) {
        if (probeProvider.priorityTaskExecutor == null) {
            return;
        }
        try {
            probeProvider.priorityTaskExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.queueFullExceptionOutput();
        }
    }
    
    /**
     * Submit task to device-threadPool.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void secondaryPriApiTaskSubmit(Runnable task) {
        if (probeProvider.secondPriTaskExecutor == null) {
            return;
        }
        try {
            probeProvider.secondPriTaskExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.queueFullExceptionOutput();
        }
    }
    
    /**
     * Return Message to CoreServer.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void outputTaskSubmit(Runnable task) {
        if (probeProvider.outputTaskExecutor == null) {
            return;
        }
        try {
            probeProvider.outputTaskExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.queueFullExceptionOutput();
        }
    }
    
    /**
     * Submit task to batchDevices-threadPool.
     * 
     * @param task - Runnable.
     * @return
     */
    public static void batchDevicesTaskSubmit(Runnable task) {
        if (probeProvider.batchDevicesExecutor == null) {
            return;
        }
        try {
            probeProvider.batchDevicesExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.exceptionOutput();
        }
    }
    
    /**
     * 
     * Description: Submit task to batchDevices-threadPool.
     *  
     * @author XXX<br>
     * @param task
     * @return <br>
     */
    public static <T> Future<T> batchDevicesTaskSubmit(Callable<T> task) {
        if (probeProvider.batchDevicesExecutor == null) {
            return null;
        }
        try {
            return probeProvider.batchDevicesExecutor.submit(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.exceptionOutput();
        }
        return null;
    }
    
    /**
     * Submit task to batchDevices-threadPools.
     * 
     * @param task - Runnable.
     * @param switchId - String 
     * @return
     */
    public static void batchDevicesExecutorsSubmit(Runnable task, String switchId) {
        try {
            probeProvider.batchDeviceExecutors.getExecutor(switchId).execute(task);
        } catch (RejectedExecutionException e) {
            ProbeApiTask<?, ?, ?> probeApiTask = (ProbeApiTask<?, ?, ?>) task;
            probeApiTask.exceptionOutput();
        }
    }
    
    /**
     * Get ThreadPool Switch Executors
     * 
     * @param
     * @return ThreadPoolSwitchExecutors<String>
     */
    public ThreadPoolSwitchExecutors<String> getThreadPoolSwitchExecutors() {
        return batchDeviceExecutors;
    }
    
    /**
     * Get SNMP服务
     * 
     * @param
     * @return SnmpService
     */
    public static SnmpService getSnmpService() {
        return snmpService;
    }
  
    /**
     * 
     * Description: 获取Probe服务
     *  
     * @author XXX<br>
     * @return <br>
     */
    public static Dview8Service getProbeService() {
        return probeService;
    }
    
    /**
     * 
     * Description: WebSocketProvider
     *  
     * @author XXX<br>
     * @return <br>
     */
    public static WebSocketProvider getWebSocketProvider() {
        return SocketKeepAlivedTask.getSocketProvider();
    }
    
    /**
     * Probe任务获取线程池初始化
     * 
     * @param 
     * @return
     */
    private void acquireApiTaskStart() {
        /* probe api task */
        UncaughtExceptionHandler probeApiUncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("acquire api task: uncaught exeception: ", throwable);
            }
        };

        BlockingQueue<Runnable> probeApiWorkQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory probeApiThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("acquire-api-task")
                .setUncaughtExceptionHandler(probeApiUncaughtExceptionHandler).build();
        acquireApiTaskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, probeApiWorkQueue,
                probeApiThreadFactory);
    }
    
    /**
     * Probe服务线程池初始化
     * 
     * @param 
     * @return
     */
    private void probeApiTaskStart() {
        /* probe api task */
        UncaughtExceptionHandler probeApiUncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("probe api task: uncaught exeception: ", throwable);
            }
        };

        BlockingQueue<Runnable> probeApiWorkQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory probeApiThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("probe-api-task")
                .setUncaughtExceptionHandler(probeApiUncaughtExceptionHandler).build();
        probeApiTaskExecutor = new ThreadPoolExecutor(PROBE_API_THREAD, PROBE_API_THREAD, 0L, TimeUnit.MILLISECONDS,
                probeApiWorkQueue, probeApiThreadFactory);
    }
    
    /**
     * 查询操作、Probe级别配置线程池初始化
     * 
     * @param 
     * @return
     */
    private void priorityTaskStart() {
        /* single task */
        UncaughtExceptionHandler deviceApiUncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("data api task: uncaught exeception: ", throwable);
            }
        };

        BlockingQueue<Runnable> deviceApiWorkQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory dataApiThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("probe-data-task")
                .setUncaughtExceptionHandler(deviceApiUncaughtExceptionHandler).build();
        priorityTaskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, deviceApiWorkQueue, dataApiThreadFactory);
    }
    
    /**
     * 单个设备操作线程池初始化
     * 
     * @param 
     * @return
     */
    private void secondPriTaskStart() {
        /* single task */
        UncaughtExceptionHandler deviceApiUncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("device api task: uncaught exeception: ", throwable);
            }
        };

        BlockingQueue<Runnable> deviceApiWorkQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory deviceApiThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("probe-dev-task")
                .setUncaughtExceptionHandler(deviceApiUncaughtExceptionHandler).build();
        secondPriTaskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, deviceApiWorkQueue,
                deviceApiThreadFactory);
    }
    
    /**
     * 向CoreServer发送消息线程池初始化
     * 
     * @param 
     * @return
     */
    private void outputTaskStart() {
        /* probe api task */
        UncaughtExceptionHandler outputUncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("output api task: uncaught exeception: ", throwable);
            }
        };

        BlockingQueue<Runnable> outputWorkQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory probeApiThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("output-api-task")
                .setUncaughtExceptionHandler(outputUncaughtExceptionHandler).build();
        outputTaskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, outputWorkQueue, probeApiThreadFactory);
    }
    
    /**
     * 批量设备操作线程池初始化，可根据设备来指定对应的执行线程
     * 
     * @param 
     * @return
     */
    private void batchDeviceExcecutorsStart() {
        batchDeviceExecutors = ThreadPoolSwitchExecutors.createExecutors("probe-dev", MAX_DEVICE_THREAD);
    }
    
    /**
     * 批量设备操作线程池初始化
     * 
     * @param 
     * @return
     */
    private void batchDevicesTaskStart() {
        LOG.info("System available Processors: {}", Runtime.getRuntime().availableProcessors());
 
        UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("probe data service task: uncaught exeception: -{}", throwable);
            }
        };

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE);
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("probe-dev-t%d")
                .setUncaughtExceptionHandler(uncaughtExceptionHandler).build();
        batchDevicesExecutor = new ThreadPoolExecutor(MIN_DEVICE_THREAD, MAX_DEVICE_THREAD, 0L, TimeUnit.MILLISECONDS,
                workQueue, threadFactory);
    }
}
