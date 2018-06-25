package com.dlink.dview8.probe;

import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.domain.model.task.Task;

/**
 * 
 * <Description> Probe业务任务执行模板
 *           <p> addElement 创建任务
 *           <p> updateElement 更新任务
 *           <p> removeElement 删除任务
 *  
 * @author Sun Tian Yu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 * @param <T>
 */
public interface ProbeElementService <T> {

    /**
     * add a element to elements container
     *
     * @param task - Task
     * @param element  - T. only fill element init info
     * @return Result K
     * statusCode - 200, 400, 404, 409, 500,reference http response code
     * tagValue   - reference http response
     * value      - if success, a struct of element
     * otherwise, null.
     */
    Result<T> addElement(Task task, T element);

    /**
     * update a element from elements container
     *
     * @param task - Task
     * @param element  - T. only fill element init info
     * @return Result T
     * statusCode - 200, 400, 404, 409, 500,reference http response code
     * tagValue   - reference http response
     * value      - if success, a struct of element
     * otherwise, null.
     */
    Result<T> updateElement(Task task, T element);

    /**
     * remove a element object from elements container.
     *
     * @param id - element uuid
     * @return Result
     * statusCode - 200, 400, 404, reference http response code
     * tagValue   - reference http response
     * value      - null
     */
    Result<Void> removeElement(String id);
    
}
