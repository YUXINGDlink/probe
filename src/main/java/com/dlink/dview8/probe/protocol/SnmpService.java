package com.dlink.dview8.probe.protocol;

import java.io.IOException;
import java.util.List;
import com.dlink.dview8.common.domain.model.discovery.SnmpCredit;


/**
 * 
 * <Description> 抽象的SNMP服务
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.protocol <br>
 */
public interface SnmpService {
    
    /**
     * 
     * Description: Get
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return String
     */
    String get(String ip, String oid, SnmpCredit credit);
    
    /**
     * 
     * Description: GetNext
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return <br>
     */
    String getNext(String ip, String oid, SnmpCredit credit);
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return String
     * @throws IOException <br>
     */
    String set(String ip, String oid, SnmpCredit credit) throws IOException ;
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return <br>
     */
    String tableView(String ip, String oid, SnmpCredit credit);
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return <br>
     */
    String walk(String ip, String oid, SnmpCredit credit);
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @return <br>
     */
    String getBulk(String ip, String oid, SnmpCredit credit);
    
    /**
     * 
     * Description: <br> 
     *  
     * @author XXX<br>
     * @param ip - String
     * @param oid - String
     * @param credit - SnmpCredit
     * @param oidList - StringList
     * @return <br>
     */
    String getList(String ip, String oid, SnmpCredit credit, List<String> oidList) ;
    
}
