package com.dlink.dview8.probe.api.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dlink.dview8.common.api.Dview8Service;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.common.domain.model.task.TaskDictionary;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> 获取CoreServer下发的任务
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月14日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.io <br>
 */
public class AcquireApiTask implements Runnable {

    /**
     * 任务类字符串
     */
    private String strTask;
    /**
     * Probe服务
     */
    private Dview8Service probeService;
    /**
     * 日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(AcquireApiTask.class);
    
    public AcquireApiTask(String task, Dview8Service probeService) {
        this.strTask = task;
        this.probeService = probeService;
    }
    
    @Override
    public void run() {
        try {
            Task inputTask = Utils.jsonStr2Obj(strTask, Task.class);
            LOG.info("New Task come, Task Type is({})", inputTask.getTaskType());
            switch (inputTask.getTaskType()) {
                case TaskDictionary.DISCOVERY_TASK: {
                    DiscoveryInput input = Utils.serializeStr2Obj(inputTask.getContext(), DiscoveryInput.class);
                    probeService.discoverDevices(input, inputTask);
                    break;
                }
                default:
                    break;
            }
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
