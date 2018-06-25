package com.dlink.dview8.probe.executors;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 
 * <Description> <br> 
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.executors <br>
 * @param <SWITCH_KEY> -
 */
public final class ThreadPoolSwitchExecutors<SWITCH_KEY> {

    /**
     * Logger instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSwitchExecutors.class);

    private String poolName;
    private int poolNum;
    private int threadNum;
    private int workQueueLen;
    private final int WORK_QUEUE_SIZE = 8000;
    private RejectedExecutionHandler rejectHandler;

    private Map<String, ThreadPoolExecutor> executors = Maps.newConcurrentMap();
    private Map<String, Long> assignCounts = Maps.newConcurrentMap();
    private Map<SWITCH_KEY, String> assigns = Maps.newConcurrentMap();
    private Map<SWITCH_KEY, Long> obtainTimestamps = Maps.newConcurrentMap();
    private Map<String, Boolean> switchState = Maps.newConcurrentMap();

    private ThreadPoolSwitchExecutors(String poolName) {
        String name = String.format("%s-timeout", poolName);

        UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("uncaught exeception: ", e);
            }
        };
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(name + "-%d")
                .setUncaughtExceptionHandler(uncaughtExceptionHandler).build();
        ScheduledThreadPoolExecutor switchKeyTimeOutThread = new ScheduledThreadPoolExecutor(1, threadFactory);
        timeStampCheck task = new timeStampCheck();
        switchKeyTimeOutThread.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS);
    }

    public static <SWITCH_KEY> ThreadPoolSwitchExecutors<SWITCH_KEY> createExecutors(String poolName, int poolNum) {
        return createExecutors(poolName, poolNum, 1);
    }

    public static <SWITCH_KEY> ThreadPoolSwitchExecutors<SWITCH_KEY> createExecutors(String poolName,
                                                                                     int poolNum, int threadNum) {
        return createExecutors(poolName, poolNum, threadNum, 0);
    }

    public static <SWITCH_KEY> ThreadPoolSwitchExecutors<SWITCH_KEY> createExecutors(String poolName, int poolNum,
                                                                                     int threadNum, int workQueueLen) {
        return createExecutors(poolName, poolNum, threadNum, workQueueLen, null);
    }

    public static <SWITCH_KEY> ThreadPoolSwitchExecutors<SWITCH_KEY> createExecutors(String poolName,
                                                                                     int poolNum, int threadNum,
                                                                                     int workQueueLen,
                                                                                     RejectedExecutionHandler reject) {
        Preconditions.checkNotNull(poolName != null && poolName.trim().length() > 0);
        Preconditions.checkNotNull(poolNum > 0);
        Preconditions.checkNotNull(threadNum > 0);

        ThreadPoolSwitchExecutors<SWITCH_KEY> executor = new ThreadPoolSwitchExecutors<SWITCH_KEY>(poolName);
        executor.poolName = poolName;
        executor.threadNum = threadNum;
        executor.poolNum = poolNum;
        executor.workQueueLen = workQueueLen;
        if (reject != null) {
            executor.rejectHandler = reject;
        } else {
            executor.rejectHandler = new AbortPolicy();
        }

        executor.startup();
        return executor;
    }

    /* 创建16个线程池，每个线程池中一个线程 ，用Map<String, ThreadPoolExecutor>存储线程池，用Map<SWITCH_KEY, String>存储线程池线程执行次数  */
    private void startup() {
        for (int i = 0; i < poolNum; i++) {
            String key = String.format("%s-p%d", poolName, i);
            UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.error("uncaught exeception: ", e);
                }
            };
            final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(key + "-t%d")
                    .setUncaughtExceptionHandler(uncaughtExceptionHandler).build();
            ThreadPoolExecutor threadExecutor = null;
            BlockingQueue<Runnable> workQueue = null;
            if (workQueueLen == 0) {
                workQueue = new LinkedBlockingQueue<Runnable>(WORK_QUEUE_SIZE);
            } else {
                workQueue = new LinkedBlockingQueue<Runnable>(workQueueLen);
            }

            threadExecutor = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS,
                    workQueue, threadFactory, rejectHandler);

            executors.put(key, threadExecutor);
            assignCounts.put(key, 0L);
        }
    }

    public void close() {
        for (String key : executors.keySet()) {
            ThreadPoolExecutor pool = executors.get(key);
            try {
                pool.shutdown();
            } catch (Exception e) {
                LOG.warn("{} shutdown executor exceptions", key, e);
            }
        }

        LOG.debug("pool[{}] has canceled", poolName);
    }

    /* 设置nodeId对应的时间戳  */
    private void refreshObtainTimestamp(SWITCH_KEY switchKey) {
        long timeStamp = System.currentTimeMillis();
        synchronized (obtainTimestamps) {
            obtainTimestamps.put(switchKey, timeStamp);
        }
    }

    /* 获取可用的线程池，assigns存放的是nodeId和线程池  */
    public ThreadPoolExecutor getExecutor(SWITCH_KEY switchKey) {
        String executorName = this.assigns.get(switchKey);
        refreshObtainTimestamp(switchKey);
        if (executorName == null) {
            return selectCandidateExecutor(switchKey);
        }
        LOG.debug("switchkey {}, get threadpoolname: {}", switchKey, executorName);
        return this.executors.get(executorName);
    }

    public ThreadPoolExecutor getExecutorByName(String executorName) {
        return this.executors.get(executorName);
    }

    public synchronized void onSwitchKeyRemoved(SWITCH_KEY switchKey) {
        String executerName = this.assigns.get(switchKey);
        this.assigns.remove(switchKey);
        LOG.debug("switchkey {}, remove from threadpoolname: {}", switchKey, executerName);
        Long count = this.assignCounts.get(executerName);
        if (count > 1) {
            count = count - 1;
        }
    }

    public synchronized void onSwitchKeyAdded(SWITCH_KEY switchKey) {
        if (this.assigns.containsKey(switchKey)) {
            return;
        }

        selectCandidateExecutor(switchKey);
    }

    /* 获取被使用最少的线程池，并累加线程池的使用次数  */
    private synchronized ThreadPoolExecutor selectCandidateExecutor(SWITCH_KEY switchKey) {
        String executorName = this.assigns.get(switchKey);
        if (executorName != null) {
            return executors.get(executorName);
        }
        Entry<String, Long> minCount = Collections.min(assignCounts.entrySet(), new Comparator<Entry<String, Long>>() {
            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        executorName = minCount.getKey();
        assigns.put(switchKey, executorName);
        assignCounts.put(executorName, minCount.getValue() + 1);
        LOG.debug("switchkey {}, get new threadpoolname: {}", switchKey, executorName);
        return executors.get(executorName);
    }

    public List<String> getAssignInfos() {
        Map<String, List<String>> caches = Maps.newHashMap();
        for (Entry<SWITCH_KEY, String> entry : assigns.entrySet()) {
            String executorName = entry.getValue();
            SWITCH_KEY switchKey = entry.getKey();
            List<String> executorAssignList = caches.get(executorName);
            if (executorAssignList == null) {
                executorAssignList = Lists.newArrayList();
                caches.put(executorName, executorAssignList);
            }
            executorAssignList.add(switchKey.toString());
        }
        List<String> list = Lists.newArrayList();
        for (Entry<String, List<String>> entry : caches.entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue().toString());
        }

        return list;
    }

    public void onSessionAdded(String switchKey) {
        LOG.debug("switch[{}] onSessionAdded", switchKey);
        synchronized (switchState) {
            switchState.put(switchKey, true);
        }
    }

    public void onSessionRemoved(String switchKey) {
        LOG.debug("switch[{}] onSessionRemoved", switchKey);
        synchronized (switchState) {
            switchState.remove(switchKey);
        }
    }

    /* 每隔5秒检查一次，如果线程池300秒内还未返回，则移除该线程池 */
    class timeStampCheck implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            synchronized (obtainTimestamps) {
                Iterator<Map.Entry<SWITCH_KEY, Long>> it = obtainTimestamps.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<SWITCH_KEY, Long> ent = it.next();
                    SWITCH_KEY key = ent.getKey();
                    //if (key instanceof String) {
                    if (switchState.get(key) != null) {
                        continue;
                    }
                    //}

                    long timeOut = now - ent.getValue().longValue();
                    if (timeOut > 300000) {
                        onSwitchKeyRemoved(ent.getKey());
                        it.remove();
                    }
                } // while
            } //synchronized
        } // run()

    }
}
