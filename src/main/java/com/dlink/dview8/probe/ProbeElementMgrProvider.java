package com.dlink.dview8.probe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.core.exception.DViewError.ErrorTag;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> Probe业务任务执行模板
 *           <p> checkElementAttr 检查业务任务输入参数
 *           <p> add2Probe 将业务任务存入Probe中
 *           <p> renderer2Fabric 与网络中的设备进行交互
 *  
 * @author Sun Tian Yu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 * @param <T>
 */
public abstract class ProbeElementMgrProvider<T> implements ProbeElementService<T> {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeElementMgrProvider.class);

    /**
     * check element attritube and alloc element resource.
     *
     * @param task - Task.
     * @param attr - T. element object.
     * @return Result T
     *         statusCode - 200, 400, 404, 409, 500,reference http response code
     *         tagValue   - reference http response
     *         value      - if success, element object
     *                       otherwise, null.
     */
    public abstract Result<T> checkElementAttr(Task task, T attr);
    
    /**
     * add element to probe.
     *
     * @param task - Task.
     * @param attr - T. element object.
     * @return Result T
     *         statusCode - 200, 400, 404, 409, 500,reference http response code
     *         tagValue   - reference http response
     *         value      - if success, element object
     *                       otherwise, null.
     */
    public abstract Result<Void> add2Probe(Task task, T attr);
    
    /**
     * remove element from probe.
     *
     * @param task - Task.
     * @param attr - T. element object.
     * @return Result T
     *         statusCode - 200, 400, 404, 409, 500,reference http response code
     *         tagValue   - reference http response
     *         value      - if success, element object
     *                       otherwise, null.
     */
    public abstract Result<Void> remove4Probe(Task task, T attr);
    
    /**
     * rendering to devices for Probe'element added
     *
     * @param task - Task.
     * @param element - T. element for rendering
     * @return Result
     *         statusCode - 200, 500,reference http response code
     *         tagValue   - reference http response
     */
    public abstract Result<T> renderer2Fabric(Task task, T element);
    
    /**
     * Result Send to CoreServer
     *
     * @param task - Task.
     * @param element - T. element for rendering
     * @return Result
     *         statusCode - 200, 500,reference http response code
     *         tagValue   - reference http response
     */
    public abstract Result<T> handleResult(Task task, T element);
    
    @Override
    public Result<T> addElement(Task task, T element) {
        LOG.debug("addElement :start");
        if (null == element || null == task) {
            LOG.error("checkElementAttr : null input");
            return Utils.failed(ErrorTag.MALFORMED_MESSAGE, "null input", null);
        }
        
        
        long startTime = System.currentTimeMillis();
        Result<T> rsCheck = checkElementAttr(task, element);
        if (!rsCheck.isSuccess()) {
            LOG.error("addElement({}) : {}", element.getClass().getSimpleName(), rsCheck.getMsg());
            return Utils.failed(ErrorTag.INVALID_VALUE, "check attritube failed");
        }
        T newElement = rsCheck.getValue();
        LOG.info("checkElementAttr time is:({})", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        
        
        Result<Void> rsDataStore = add2Probe(task, newElement);
        if (!rsDataStore.isSuccess()) {
            LOG.error("addElement({}) : {}", newElement.getClass().getSimpleName(), rsDataStore.getMsg());
            return Utils.failed(rsDataStore, null);
        }
        LOG.info("add2Probe time is:({})", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        
        
        Result<T> rs = renderer2Fabric(task, newElement);
        if (!rs.isSuccess()) {
            LOG.error("addElement({}) : {}", newElement.getClass().getSimpleName(), rs.getMsg());
            remove4Probe(task, newElement);
            return Utils.failed(rs, null);
        }
        T result = rs.getValue();
        LOG.info("renderer2Fabric time is:({})", System.currentTimeMillis() - startTime);
        
        rs = handleResult(task, result);
        if (!rs.isSuccess()) {
            LOG.error("addElement({}) : {}", newElement.getClass().getSimpleName(), rs.getMsg());
            remove4Probe(task, newElement);
            return Utils.failed(rs, null);
        }
        result = rs.getValue();
        LOG.info("resultHandle time is:({})", System.currentTimeMillis() - startTime);
        
        LOG.debug("addElement :finished");
        return Utils.success(task.getTaskId(), result);
    }

    /**
     * 
     * Description: <br> 
     *  
     * @author XXX <br>
     * @param taskId
     * @param element
     * @return <br>
     */
    @Override
    public Result<T> updateElement(Task task, T element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> removeElement(String id) {
        // TODO Auto-generated method stub
        return null;
    }


}
