package com.dlink.dview8.probe.north.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.drafts.Draft_6455;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.common.domain.model.task.TaskDictionary;
import com.dlink.dview8.common.utils.Utils;


/**
 * 
 * <Description> WebSocket链接保活
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月13日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.io <br>
 */
public class SocketKeepAlivedTask implements Runnable {

    /**
     * 日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(SocketKeepAlivedTask.class);
    /**
     * URL
     */
    private String url;
    /**
     * flag
     */
    private static boolean isClosed = false;
    /**
     * WebSocketProvider
     */
    private static AtomicReference<WebSocketProvider> atomicSocket = new AtomicReference<WebSocketProvider>();
    
    public SocketKeepAlivedTask(String url) {
        this.url = url;
    }
    
    @Override
    public void run() {
        WebSocketProvider webSocketProvider = null;
        try {
            webSocketProvider = new WebSocketProvider(new URI(url), new Draft_6455(), null, 0);
            atomicSocket.set(webSocketProvider);
        } catch (URISyntaxException e2) {
            
        }
        
        // TODO 采用读配置文件方式获得ProbeID
        String probeId = "00000000-0000-0000-0000-000000000000";
        Task task = new Task();
        task.setProbeId(probeId);
        task.setTaskId(probeId);
        task.setTaskType(TaskDictionary.PROBE_ONLINE_TASK);
        String taskStr = Utils.obj2JsonStr(task);
        
        while(!isClosed) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            try {
                if (webSocketProvider != null && !webSocketProvider.getReadyState().equals(READYSTATE.OPEN)) {
                    webSocketProvider.connect();
                    
                } else if (webSocketProvider != null && !webSocketProvider.isProbeSocketReady()) {
                    webSocketProvider.send(taskStr);
                }
                
            } catch (Throwable e) {
                LOG.error("ProbeIO uncaught exception: ", e);
                try {
                    webSocketProvider = new WebSocketProvider(new URI(url), new Draft_6455(), null, 0);
                    webSocketProvider.connect();
                    atomicSocket.getAndSet(webSocketProvider);
                } catch (URISyntaxException e1) {
                }
            }
        }
    }

    /**
     * 
     * Description: 获取WebSocket服务
     *  
     * @author XXX<br>
     * @return <br>
     */
    public static WebSocketProvider getSocketProvider() {
        return atomicSocket.get();
    }

    public static boolean isClosed() {
        return isClosed;
    }

    public static void setClosed(boolean close) {
        isClosed = close;
    }
    
}
