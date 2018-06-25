package com.dlink.dview8.probe.service;

import java.util.concurrent.Future;

import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryOutput;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.api.task.AddDiscoveryApiTask;

/**
 * 
 * <Description> Probe对外接口服务实现
 *  
 * @author SunTianYU <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.service <br>
 */
public class ProbeApiProvider extends ProbeService {

    @Override
    public Future<Result<DiscoveryOutput>> discoverDevices(DiscoveryInput input, Task inputTask) {
        AddDiscoveryApiTask task = new AddDiscoveryApiTask();
        Future<Result<DiscoveryOutput>> future = task.create(input, inputTask);
        ProbeProvider.probeApiTaskSubmit(task);
        return future;
    }
}