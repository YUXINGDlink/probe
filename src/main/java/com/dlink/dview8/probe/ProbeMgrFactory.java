package com.dlink.dview8.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <Description> Probe业务管理类工厂
 *  
 * @author SunTianyu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 */
public class ProbeMgrFactory {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeMgrFactory.class);

    /**
     * 基础服务
     */
    protected static Map<Class<?>,  ProbeElementService<?>> mgrServices = new HashMap<Class<?>,  ProbeElementService<?>>();
    
    /**
     * 
     */
    protected static List<Class<?>> mgrSeqs = new ArrayList<Class<?>>();
   
    /**
     * 基础服务
     */
    protected static Map<Class<?>,  ProbeElementService<?>> eleMaps = new HashMap<Class<?>,  ProbeElementService<?>>();
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param eleCls
     * @param mgrCls <br>
     */
    public static void create(Class<?> eleCls, Class<?> mgrCls) {

        if (null == mgrCls || null == eleCls) {
            return;
        }

        try {
            mgrSeqs.add(mgrCls);
            ProbeElementService<?> serv = (ProbeElementService<?>) mgrCls.newInstance();
            mgrServices.put(mgrCls, serv);
            eleMaps.put(eleCls, serv);

        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("create fabric manager error ({})", e);
            e.printStackTrace();
        }
    }

    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br> <br>
     */
    public static void clear() {
        mgrSeqs.clear();
        mgrServices.clear();
        eleMaps.clear();
    }

    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @return <br>
     */
    public static List<ProbeElementService<?>> getServices() {
        List<ProbeElementService<?>> services = new ArrayList<ProbeElementService<?>>();
        for (Class<?> type: mgrSeqs) {
            ProbeElementService<?> serv = mgrServices.get(type);
            if (null != serv) {
                services.add(serv);
            }
        }
        return services;
    }

    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param eleCls - 
     * @return <br>
     */
    public static ProbeElementService<?> getService(Class<?> eleCls) {
        if (null == eleCls) {
            return null;
        }

        ProbeElementService<?> serv = eleMaps.get(eleCls);
        return serv;
    }

    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param mgrCls -
     * @return <br>
     */
    public static ProbeElementService<?> getServiceByMgr(Class<?> mgrCls) {
        if (null == mgrCls) {
            return null;
        }

        ProbeElementService<?> serv = mgrServices.get(mgrCls);
        return serv;
    }
}
