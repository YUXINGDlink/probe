package com.dlink.dview8.probe.north.io;

import com.dlink.dview8.probe.ProbeProvider;

/**
 * 
 * <Description> 向CoreServer输出结果
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月14日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.io <br>
 */
public class OutputTask implements Runnable {

    /**
     * 发送给Coreserver的消息
     */
    private String message;
    
    public OutputTask(String message) {
        this.message = message;
    }
    
    @Override
    public void run() {
        if (message == null) {
            return;
        }
        ProbeProvider.getWebSocketProvider().send(message);
    }

}
