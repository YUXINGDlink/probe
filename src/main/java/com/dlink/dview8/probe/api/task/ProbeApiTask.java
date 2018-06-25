package com.dlink.dview8.probe.api.task;

import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.core.exception.DViewError.ErrorTag;
import com.dlink.dview8.common.utils.Utils;
import com.google.common.util.concurrent.SettableFuture;

/**
 * 
 * <Description> Probe Api 任务模板
 *  
 * @author Sun Tian Yu
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.task <br>
 * @param <T> -
 * @param <N> -
 * @param <C> -
 */
public abstract class ProbeApiTask<T, N, C> implements Runnable {

    /**
     * 
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeApiTask.class);
    /**
     * 
     */
    public T input;
    /**
     * 任务
     */
    protected C inputTask;
    /**
     * 
     */
    private SettableFuture<Result<N>> future;
    /**
     * 
     */
    private boolean debug = false;

    /**
     * 
     * Description: set parameter
     *  
     * @author XXX<br>
     * @param input - T
     * @return <br>
     */
    public Future<Result<N>> create(T input, C inputTask) {
        LOG.info("ProbeApiTask:input:{}", input);
        this.future = SettableFuture.<Result<N>>create();
        this.input = input;
        this.inputTask = inputTask;
        return this.future;
    }

    /**
     * 
     * Description: set parameter
     *  
     * @author XXX<br>
     * @param input - T
     * @param useLogDebug - boolean
     * @return <br>
     */
    public Future<Result<N>> create(T input, boolean useLogDebug) {
        LOG.debug("ProbeApiTask:input:{}", input);
        this.future = SettableFuture.<Result<N>>create();
        this.input = input;
        this.debug = useLogDebug;
        return this.future;
    }

    /**
     * 
     * Description: set future result
     *  
     * @author XXX<br>
     * @param out - Result
     */
    public void setOutput(Result<N> out) {
        if (out != null) {
            if (out.isSuccess()) {
                if (!debug) {
                    LOG.info("ProbeApiTask:setOutput:{}", out);
                } else {
                    LOG.debug("ProbeApiTask:setOutput:{}", out);
                }
            } else {
                LOG.error("ProbeApiTask:setOutput:{}", out);
            }
        }
        getFuture().set(out);
        return;
    }

    public SettableFuture<Result<N>> getFuture() {
        return this.future;
    }

    /** 
     * 
     * Description: <br> 
     *  
     * @author XXX<br> <br>
     */
    public void exceptionOutput() {
        LOG.error("exceptionOutput");
        Result<N> out = Utils.failed(ErrorTag.OPERATION_FAILED, "exception");
        getFuture().set(out);
        return;
    }
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br> <br>
     */
    public void queueFullExceptionOutput() {
        LOG.error("probe service queue is full");
        Result<N> out = Utils.failed(ErrorTag.ACCESS_DENIED, "probe service is busy");
        getFuture().set(out);
        return;
    }

    /**
     * 
     * Description: 任务真正执行的地方
     *  
     * @author XXX<br>
     * @return <br>
     */
    public abstract Result<N> call();

    @Override
    public void run() {
    	Result<N> result = null;
        try {
            result = call();
        }
        catch (Throwable e) {
            result = Utils.failed(ErrorTag.OPERATION_FAILED, "exception");
            LOG.error("uncaught exception: ", e);
        }
        finally {
            setOutput(result);
        }
    }
}
