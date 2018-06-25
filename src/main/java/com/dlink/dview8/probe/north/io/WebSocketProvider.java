package com.dlink.dview8.probe.north.io;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlink.dview8.common.api.Dview8Service;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.api.task.AcquireApiTask;

/**
 * 
 * <Description> WebSocketProvider 
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月14日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.io <br>
 */
public class WebSocketProvider extends WebSocketClient {
    
    /**
     * 日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketProvider.class);
    /**
     * 判断Probe是否就绪
     */
    private volatile boolean isReady = false;
    /**
     * 授权码
     */
    private static final String AUTH_COMPLETE = "AUTH COMPLETE";
    /**
     * 获取Probe服务
     */
    Dview8Service probeService = ProbeProvider.getProbeService();
    
    /**
     * 
     * @param serverUri
     * @param protocolDraft
     * @param httpHeaders
     * @param connectTimeout
     */
    public WebSocketProvider(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {  
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);  
    }  
  
    @Override  
    public void onOpen(ServerHandshake arg0) {  
        LOG.info("WebSocket Connect Success!");
    }  
  
    @Override  
    public void onMessage(String message) {
        if (message == null) {
            return;
        }
        if (message.contains(AUTH_COMPLETE)) {
            isReady = true;
            return;
        }
        if (isReady == false) {
            return;
        }
        LOG.info("WebSocket Received Msg:({})", message);
        AcquireApiTask msgTask = new AcquireApiTask(message, probeService);
        ProbeProvider.acquireApiTaskSubmit(msgTask);
    }  
  
    @Override  
    public void onError(Exception ex) {  
        if (ex != null) {
            isReady = false;
            LOG.error("WebSocket Meet Error Exception:({})", ex);
        }
    }  
  
    @Override  
    public void onClose(int arg0, String arg1, boolean arg2) {
        isReady = false;
        LOG.info("WebSocket Closed.");
    }  
  
    @Override  
    public void onMessage(ByteBuffer bytes) {  
        if (bytes != null) {
            LOG.info("WebSocket Received Msg:({})", bytes);
        }
    }
    
    public boolean isProbeSocketReady() {
        return isReady;
    }

}
