package com.dlink.dview8.probe.service;

import java.util.concurrent.Future;
import com.dlink.dview8.common.api.Dview8Service;
import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.core.exception.DViewError.ErrorTag;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryOutput;
import com.dlink.dview8.common.domain.model.system.network.Network;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> <br> 
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月21日 <br>
 * @since V1.0 <br>
 * @see com.dlink.dview8.probe.service <br>
 */
public abstract class ProbeService implements Dview8Service {

    @Override
    public Future<Result<Void>> addNetwork(Network input, Task inputTask) {
        return Utils.buildFailedFuture(ErrorTag.OPERATION_NOT_SUPPORTED, "not supported");
    }
    

    @Override
    public Future<Result<DiscoveryOutput>> listDiscoverDevices(String networkId, Task inputTask) {
        return Utils.buildFailedFuture(ErrorTag.OPERATION_NOT_SUPPORTED, "not supported");
    }
 
    @Override
    public Future<Result<Task>> getTaskById(String taskId, Void nothing) {
        return Utils.buildFailedFuture(ErrorTag.OPERATION_NOT_SUPPORTED, "not supported");
    }

}
