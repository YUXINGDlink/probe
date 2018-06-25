package com.dlink.dview8.probe.discovery;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryDevice;
import com.dlink.dview8.common.domain.model.discovery.SnmpCredit;
import com.dlink.dview8.common.utils.IpUtils;
import com.dlink.dview8.probe.ProbeManagers;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.protocol.SnmpService;
import com.google.common.collect.Lists;

/**
 * 
 * <Description> 设备发现实现
 *  
 * @author SunTianyu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.discovery <br>
 */
public class DeviceDiscoveryTask implements Callable<DiscoveryDevice> {
    /**
     *  指定设备IP 
     */
    private String ip = null;
    
    /**
     *  相位器 
     */
    private Phaser phaser = null;
    
    /**
     *  SNMP证书 
     */
    private SnmpCredit credit = null;
    
    /**
     *  日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDiscoveryTask.class);
    
    /**
     *  发现mib字典
     */
    private Map<String, String> mibDictionaryMap =
            ProbeManagers.getManager(ProbeDiscoveryMgrProvider.class).getMibDictionaryMap();
    
    /**
     * 系统信息 oid 
     */
    private static final String SYSTEM_INFO_OID = ".1.3.6.1.2.1.1";

    /**
     *  Bridge Mac oid 
     */
    private static final String BRIDGE_ADDRESS_OID = ".1.3.6.1.2.1.17.1.1.0";
    
    /**
     *  Port Sum oid 
     */
    private static final String NUM_PORTS = ".1.3.6.1.2.1.17.1.2.0";
    
    /**
     *  ifPhysAddress oid 
     */
    private static final String IF_PHYS_ADDRESS = ".1.3.6.1.2.1.2.2.1.6.1";
    
    @Override
    public DiscoveryDevice call() {
//        long startTime = System.currentTimeMillis();
        DiscoveryDevice device = null;
        try {
            device = pingDiscover(this.ip);
            if (device != null) {
                device = snmpDiscovery(this.ip, credit, device);
            }
            
            if (phaser != null) {
                phaser.arriveAndDeregister();
//                LOG.info("phase: {}, ---, {}, time is ({})", phaser.getPhase(), 
//                        phaser.getRegisteredParties(), System.currentTimeMillis() - startTime);
            }
        } catch (Throwable e) {
            LOG.error("DiscoveryDevice Uncaught Exception: ", e);
            if (phaser != null) {
                phaser.arriveAndDeregister();
            }
        }
   
        return device;
    }
    
    /**
     * 
     * Description: Ping探测设备
     *  
     * @author XXX<br>
     * @param ip - String
     * @return <br>
     */
    private DiscoveryDevice pingDiscover(String ip) {
        DiscoveryDevice device = null;
        Long begin = System.currentTimeMillis();
        boolean reachable = IpUtils.isIpReachable(ip);
        Long delayTm = (System.currentTimeMillis() - begin);
        if (delayTm < 1) {
            delayTm = new Long(1);
        }
        if (reachable) {
            device = new DiscoveryDevice();
            device.setIpAddr(ip);
            device.setResponseTime(delayTm.intValue());
        }
        return device;
    }
    
    /**
     * 
     * Description: Snmp发现设备
     *  
     * @author XXX<br>
     * @param ip - String
     * @param credit - SnmpCredit
     * @param device - DiscoveryDevice
     * @return <br>
     */
    private DiscoveryDevice snmpDiscovery(String ip, SnmpCredit credit, DiscoveryDevice device) {
  
        // 设备基本信息发现
        SnmpService snmpService = ProbeProvider.getSnmpService();
        // 读设备系统信息
        String sysInfo = snmpService.getBulk(ip, SYSTEM_INFO_OID, credit);
        if (sysInfo == null || sysInfo.isEmpty() || sysInfo.contains("Error")) {
            return null;
        }
        device = buildDeviceByGetBulk(device, sysInfo);
        // 读设备MAC
        String mac = snmpService.get(ip, BRIDGE_ADDRESS_OID, credit);
        if (mac == null || sysInfo.isEmpty() || mac.contains("Error")) {
            mac = snmpService.getNext(ip, IF_PHYS_ADDRESS, credit);
        }
        device = buildDeviceByGet(device, mac);
        // TODO MAC发现读LLDP Classic
        
        // 读设备端口总数
        String portCount = snmpService.get(ip, NUM_PORTS, credit);
        device = buildDeviceByGet(device, portCount);
        // TODO 匹配DeviceIdentify.josn中的设备列表，如果是有线网络设备，还需要读设备的端口信息
        
        // TODO 设备端口信息发现
        
        // TODO 设备LLDP、FDB信息发现
        
        return device;
    }

    /**
     * 
     * Description: 由Snmp Get到的信息构建设备详情
     *  
     * @author XXX<br>
     * @param device - DiscoveryDevice
     * @param info - String
     * @return <br>
     */
    private DiscoveryDevice buildDeviceByGet(DiscoveryDevice device, String info) {
        if (info == null) {
            return device;
        }
        
        StringTokenizer stEle = new StringTokenizer(info, "=", false);
        String key = null;
        while (stEle.hasMoreElements()) {
           
            String snmpValue = stEle.nextToken().trim();
            if (key == null) {
                key = snmpValue;
            }
            
            String value = mibDictionaryMap.get(key);
            if (value != null && value.equals("bridgeAddress")) {
                device.setMacAddr(snmpValue);
            }
            if (value != null && value.equals("ifPhysAddress")) {
                device.setMacAddr(snmpValue);
            }
            if (value != null && value.equals("numPorts")) {
                device.setPortCount(Integer.parseInt(snmpValue));
            }
        }
        return device;
    }
    
    /**
     * 
     * Description:  由Snmp GetBulk到的信息构建设备详情
     *  
     * @author XXX<br>
     * @param device - DiscoveryDevice
     * @param snmpInfo - String
     * @return <br>
     */
    private DiscoveryDevice buildDeviceByGetBulk(DiscoveryDevice device, String snmpInfo) {
        if (snmpInfo == null) {
            return device;
        }
        
        // 分词器构造函数三个参数，第一个是待分隔的字符串，第二个为分隔字符串，以字符为分隔单位（比如the，可能匹配到e，就会分隔），
        //第三个参数说明是否要把分割字符串作为标记返回
        List<String> sysInfos = Lists.newArrayList();
        StringTokenizer st = new StringTokenizer(snmpInfo, "|", false);
        
        while (st.hasMoreElements()) {
            sysInfos.add(st.nextToken());
        }
        
        for (String ele : sysInfos) {
            StringTokenizer stEle = new StringTokenizer(ele, "=", false);
            String key = null;
            while (stEle.hasMoreElements()) {
               
                String snmpValue = stEle.nextToken().trim();
                if (key == null) {
                    key = snmpValue;
                }
                
                String value = mibDictionaryMap.get(key);
                if (value != null && value.equals("sysDescription")) {
                    device.setSysDescription(snmpValue);
                }
                if (value != null && value.equals("sysObjectId")) {
                    device.setSysObjectId(snmpValue);
                }
                if (value != null && value.equals("sysUpTime")) {
                    device.setSysUpTime(snmpValue);
                }
                if (value != null && value.equals("sysContact")) {
                    
                }
                if (value != null && value.equals("sysName")) {
                    device.setSysName(snmpValue);
                }
                if (value != null && value.equals("sysLocation")) {
                    
                }
                if (value != null && value.equals("sysServices")) {
                    
                }
                if (value != null && value.equals("bridgeAddress")) {
                    device.setMacAddr(snmpValue);
                }
                if (value != null && value.equals("numPorts")) {
                    
                }
                if (value != null && value.equals("ifPhysAddress")) {
                    
                }
            }
        }
        
        return device;
    }
    
    DeviceDiscoveryTask(String ip) {
        this.ip = ip;
    }
    
    DeviceDiscoveryTask(String ip, SnmpCredit credit) {
        this.ip = ip;
        this.credit = credit;
    }
    
    DeviceDiscoveryTask(String ip, Phaser phaser) {
        this.ip = ip;
        this.phaser = phaser;
    }

    DeviceDiscoveryTask(String ip, SnmpCredit credit, Phaser phaser) {
        this.ip = ip;
        this.credit = credit;
        this.phaser = phaser;
    }
}
