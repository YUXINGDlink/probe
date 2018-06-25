package com.dlink.dview8.probe;

import java.util.List;
/**
 * 
 * <Description> 维护Porbe业务管理类
 *  
 * @author SunTianyu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 */
public class ProbeManagers {
    /**
     * 
     * Description: 创建
     *  
     * @author XXX<br>
     * @param eleCls - 
     * @param mgrCls <br>
     */
    public static void create(Class<?> eleCls, Class<?> mgrCls) {
        ProbeMgrFactory.create(eleCls, mgrCls);
    }

    /**
     * 
     * Description: 清除
     *  
     * @author XXX<br> <br>
     */
    public static void clear() {
        ProbeMgrFactory.clear();
    }

    public static List<ProbeElementService<?>> getServices() {
        return ProbeMgrFactory.getServices();
    }

    /**
     * 
     * Description: 获取ProbeElementService服务
     *  
     * @author XXX<br>
     * @param eleCls -
     * @param <K> -
     * @return <br>
     */
    @SuppressWarnings("unchecked")
    public static <K> ProbeElementService<K> getService(Class<K> eleCls) {
        return (ProbeElementService<K>) ProbeMgrFactory.getService(eleCls);
    }

    /**
     * 
     * Description: 获取具体业务管理类服务
     *  
     * @author XXX<br>
     * @param mgrCls -
     * @param <K> -
     * @return <br>
     */
    @SuppressWarnings("unchecked")
    public static <K> K getManager(Class<K> mgrCls) {
        return (K)ProbeMgrFactory.getServiceByMgr(mgrCls);
    }

    /**
     * 
     * Description: 获取ProbeElementMgrProvider服务
     *  
     * @author XXX<br>
     * @param eleCls -
     * @param <K> -
     * @return <br>
     */
    @SuppressWarnings("unchecked")
    public static <K> ProbeElementMgrProvider<K> getEleMgr(Class<K> eleCls) {
        return (ProbeElementMgrProvider<K>)ProbeMgrFactory.getService(eleCls);
    }
}
