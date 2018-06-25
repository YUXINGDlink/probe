package com.dlink.dview8.probe.api.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.core.exception.DViewError.ErrorTag;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryOutput;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.probe.ProbeManagers;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.discovery.ProbeDiscoveryMgrProvider;
import com.dlink.dview8.probe.discovery.model.DiscoveryTask;
import com.dlink.dview8.probe.north.io.OutputTask;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> AddDiscoveryApiTask
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.task <br>
 */
public class AddDiscoveryApiTask extends ProbeApiTask<DiscoveryInput, DiscoveryOutput, Task> {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(AddDiscoveryApiTask.class);
    
    @Override
    public Result<DiscoveryOutput> call() {
        long startTime = System.currentTimeMillis();
        if (null == input) {
            return Utils.failed(ErrorTag.INVALID_VALUE, "Input is null");
        }

        DiscoveryTask discoveryInput = ProbeManagers.getManager(ProbeDiscoveryMgrProvider.class).buildDiscoveryTask(input);
        Result<DiscoveryTask> rs = ProbeManagers.getService(DiscoveryTask.class).addElement(inputTask, discoveryInput);
        if (!rs.isSuccess()) {
            LOG.error("Discovery Failed, -{}", rs.getMsg());
            handleFailed(rs);
            return Utils.failed(rs, null);
        }
        DiscoveryOutput output = new DiscoveryOutput();
        if (rs.getValue() != null) {
            output.setDevices(rs.getValue().getDevices());
        }
        
        LOG.info("Discovery End Time is ({})ms", System.currentTimeMillis() - startTime);
        return Utils.success(output);
    }
    /**
     * 
     * Description: 处理失败结果，失败提示发给CoreServer
     *  
     * @author XXX<br>
     * @param rs <br>
     */
    private void handleFailed(Result<DiscoveryTask> rs) {
        inputTask.setErrorCode(rs.getCode());
        inputTask.setErrorMessage(rs.getMsg());
        inputTask.setTaskStatus("failed");
        String strSend= Utils.obj2JsonStr(inputTask);
        OutputTask outputTask = new OutputTask(strSend);
        ProbeProvider.outputTaskSubmit(outputTask);
    }
}
