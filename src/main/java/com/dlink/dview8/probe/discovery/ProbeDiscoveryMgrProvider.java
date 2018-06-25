package com.dlink.dview8.probe.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dlink.dview8.probe.ProbeElementMgrProvider;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.discovery.model.DiscoveryTask;
import com.dlink.dview8.probe.north.io.OutputTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.core.exception.DViewError.ErrorTag;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryDevice;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryOutput;
import com.dlink.dview8.common.domain.model.discovery.IpRange;
import com.dlink.dview8.common.domain.model.discovery.SnmpCredit;
import com.dlink.dview8.common.domain.model.task.Task;
import com.dlink.dview8.common.utils.IpUtils;
import com.dlink.dview8.common.utils.Utils;

/**
 * 
 * <Description> Probe 发现业务管理类
 *  
 * @author SunTianYu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.discovery <br>
 */
public class ProbeDiscoveryMgrProvider extends ProbeElementMgrProvider<DiscoveryTask> {
    
    /**
     *  发现方式 
     */
    private static final String RANGE_METHOD = "range";
    /**
     *  发现方式 
     */
    private static final String PROTOOL_METHOD = "protocol";
    
    /**
     *  最大可管理的设备数量 
     */
    private static final int MAX_DEVICES_SIZE = 5000;
    
    /**
     *  解析OID对应的是哪个具体名称 
     */
    private Map<String, String> mibDictionaryMap = Maps.newHashMap();
    
    /**
     *  单次发现任务集合 
     */
    private Map<String, DiscoveryTask> singleDiscoveryTask = Maps.newHashMap();
    
    /**
     *  周期性发现任务集合
     */
    private Map<String, DiscoveryTask> periodicDiscoveryTask = Maps.newHashMap();
    
    /**
     *  日志 
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeProvider.class);
    
    /**
     * 
     * Description: Mib发现字典初始化 
     *  
     * @author XXX<br> <br>
     */
    private void mibDictionaryInit() {
        
        mibDictionaryMap.put("1.3.6.1.2.1.1.1.0", "sysDescription");
        mibDictionaryMap.put("1.3.6.1.2.1.1.2.0", "sysObjectId");
        mibDictionaryMap.put("1.3.6.1.2.1.1.3.0", "sysUpTime");
        mibDictionaryMap.put("1.3.6.1.2.1.1.4.0", "sysContact");
        mibDictionaryMap.put("1.3.6.1.2.1.1.5.0", "sysName");
        mibDictionaryMap.put("1.3.6.1.2.1.1.6.0", "sysLocation");
        mibDictionaryMap.put("1.3.6.1.2.1.1.7.0", "sysServices");
        mibDictionaryMap.put("1.3.6.1.2.1.17.1.1.0", "bridgeAddress");
        mibDictionaryMap.put("1.3.6.1.2.1.17.1.2.0", "numPorts");
        mibDictionaryMap.put("1.3.6.1.2.1.2.2.1.6.1", "ifPhysAddress");
    }
    
    public ProbeDiscoveryMgrProvider() {
        // 初始化mib字典
        mibDictionaryInit();
        /*  
         * 初始化周期性发现定时器
         * 
         * 这里的实现方式有两种：
         *  <1>使用定时器，每隔一定时间扫描周期性任务列表，判断是否到了执行时间
         *  <2>单独开一个线程，使用Sleep方法，每隔一定时间扫描周期性任务列表
         *  <3>是否有这样一种实现：给定时器设置指定日期，让其周期性触发
         * 
         * */
        
    }
    
    @Override
    public Result<DiscoveryTask> checkElementAttr(Task task, DiscoveryTask attr) {
        if (attr.getIpCount() <= 0 || attr.getIpCount() > 5000) {
            Result<Integer> rsInt = checkInputIpRange(attr);
            if (!rsInt.isSuccess()) {
                return Utils.failed(ErrorTag.INVALID_VALUE, rsInt.getMsg());
            }
            attr.setIpCount(rsInt.getValue().intValue());
        }
        
        return Utils.success(attr);
    }

    @Override
    public Result<Void> add2Probe(Task taskInput, DiscoveryTask attr) {
        
        if (attr.getDiscoveryType() != null && attr.getDiscoveryType().equals("single")) {
            singleDiscoveryTask.put(taskInput.getTaskId(), attr);
        } else {
            periodicDiscoveryTask.put(taskInput.getTaskId(), attr);
        }
        
        return Utils.success(null);
    }

    @Override
    public Result<Void> remove4Probe(Task task, DiscoveryTask attr) {
        return Utils.success(null);
    }

    @Override
    public Result<DiscoveryTask> renderer2Fabric(Task task, DiscoveryTask element) {
        if (element.getDiscoveryMethod() == null) {
            LOG.error("DiscoveryTask'Method is null, -{}", Utils.getLineInfo());
            return Utils.failed(ErrorTag.INVALID_VALUE, "DiscoveryTask'Method is null");
        }
        List<DiscoveryDevice> devices = Lists.newArrayList();
        /* 选择发现方式 */
        switch(element.getDiscoveryMethod()) {
            case RANGE_METHOD: {
                devices.addAll(discoverByRange(element));
                break;
            }
            case PROTOOL_METHOD: {
                // TODO 网络协议发现设备方式
                break;
            }
            default:
                break;
        }
        element.setDevices(devices);
        
        return Utils.success(element);
    }
    
    @Override
    public Result<DiscoveryTask> handleResult(Task task, DiscoveryTask element) {
        DiscoveryOutput output = new DiscoveryOutput();
        output.setDevices(element.getDevices());
        
        String strContext = null;
        try {
            strContext = Utils.obj2SerializeStr(output);
//            byte[] bs = SerializationUtils.serialize(output);
//            DiscoveryOutput kk = (DiscoveryOutput)SerializationUtils.deserialize(bs);
//            strContext = new String (bs);
        } catch (IOException e) {
            // TODO 异常处理
            e.printStackTrace();
            LOG.error("DiscoveryOutput 序列化出现异常. -{}", Utils.getLineInfo());
        }
        task.setResult(strContext);
        task.setTaskStatus("success");
        String strSend= Utils.obj2JsonStr(task);
        
        OutputTask outputTask = new OutputTask(strSend);
        ProbeProvider.outputTaskSubmit(outputTask);
        
        return Utils.success(element);
    }
    
    // TODO IP重复性检测
    /**
     * Description: 校验输入IP的合法性
     *  
     * @author XXX<br>
     * @param input - DiscoveryOutput
     * @return <br>
     */
    private Result<Integer> checkInputIpRange(DiscoveryTask input) {
        int count = 0;
        /* 校验指定IP合法性 */
        if (input.getIp() != null) {
            if (IpUtils.isIPAddress(input.getIp())) {
                count += 1;
            } else {
                LOG.error("Input IP is Invaild! -{}", Utils.getLineInfo());
                return Utils.failed(ErrorTag.INVALID_VALUE, "Input IP is invaild!");
            }
        }
        
        /* 校验指定IP-Range合法性 */
        long ipStart = 0;
        long ipEnd = 0;
        List<IpRange> ipRanges = input.getIpRanges();
        if (!Utils.isEmpty(ipRanges)) {
            for (IpRange ipRange : ipRanges) {
                if (ipRange == null || ipRange.getStartIp() == null || ipRange.getEndIp() == null) {
                    LOG.error("ipRange is null! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "ipRange is null!");
                }
                if (!IpUtils.isIPAddress(ipRange.getStartIp()) || !IpUtils.isIPAddress(ipRange.getEndIp())) {
                    LOG.error("ipRange is null! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "Input IP is invaild!");
                }

                // change StringIP 2 LongIP
                try {
                    ipStart = IpUtils.getIP(InetAddress.getByName(ipRange.getStartIp()));
                    ipEnd = IpUtils.getIP(InetAddress.getByName(ipRange.getEndIp()));
                } catch (UnknownHostException e1) {
                    LOG.error("Change StringIP 2 LongIP Error! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "Change StringIP 2 LongIP Error!");
                }
                if (ipStart > ipEnd) {
                    LOG.error("IP Range: StartIP is bigger than EndIP! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "IP Range: StartIP is bigger than EndIP!");
                }

                // check ips Zone size
                int size = (int) (ipEnd - ipStart) + 1;
                count += size;
            }
        }
        
        /* 校验指定Cidrs合法性 */
        if (!Utils.isEmpty(input.getCidrs())) {
            for (String cidr : input.getCidrs()) {
                String ipStrStart = IpUtils.getMinIpByCidr(cidr);
                String ipStrEnd = IpUtils.getMaxIpByCidr(cidr);
                try {
                    ipStart = IpUtils.getIP(InetAddress.getByName(ipStrStart));
                    ipEnd = IpUtils.getIP(InetAddress.getByName(ipStrEnd));
                } catch (Exception e) {
                    LOG.error("Change StringIP 2 LongIP Error! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "Change StringIP 2 LongIP Error!");
                }
                // check ips Zone size
                int size = (int) (ipEnd - ipStart) + 1;
                count += size;
            }
        }
        
        /* 校验指定IP列表合法性 */
        if (!Utils.isEmpty(input.getIps())) {
            for (String ele : input.getIps()) {
                if (IpUtils.isIPAddress(ele)) {
                    count += 1;
                } else {
                    LOG.error("Input IP is Invaild! -{}", Utils.getLineInfo());
                    return Utils.failed(ErrorTag.INVALID_VALUE, "Input IP is invaild!");
                }
            }
        }
        
        return Utils.success(Integer.valueOf(count));
    }
    
    /**
     * 
     * Description: 基于IP范围的发现
     *  
     * @author XXX<br>
     * @param input - DiscoveryTask
     * @return <br>
     */
    private List<DiscoveryDevice> discoverByRange(DiscoveryTask input) {
        
        List<DiscoveryDevice> deviceList = Lists.newArrayList();
        int ipCount = input.getIpCount();
        LOG.info("This Discovery ipCount is ({})", ipCount);
        
        // detect device whether is reachable
        Phaser phaser = new Phaser(ipCount + 1);
        List<Future<DiscoveryDevice>> futureDeviceList = Lists.newArrayList();
        
        /* 指定IP地址 */
        if (input.getIp() != null) {
            DeviceDiscoveryTask devDiscoverTask = new DeviceDiscoveryTask(input.getIp(), input.getSnmpCredit(), phaser);
            Future<DiscoveryDevice> futureDev = ProbeProvider.batchDevicesTaskSubmit(devDiscoverTask);
            if (futureDev != null) {
                futureDeviceList.add(futureDev);
            }
        }
        
        /* 指定IP Range */
        if (!Utils.isEmpty(input.getIpRanges())) {
            for (IpRange ipRange : input.getIpRanges()) {
                goThroughIpRange(ipRange.getStartIp(), ipRange.getEndIp(), input.getSnmpCredit(), phaser, futureDeviceList);
            }
        }
        
        // TODO 实现CIDR范围内的设备发现
        if (!Utils.isEmpty(input.getCidrs())) {
            for (String ele : input.getCidrs()) {
                String ipStrStart = IpUtils.getMinIpByCidr(ele);
                String ipStrEnd = IpUtils.getMaxIpByCidr(ele);
                goThroughIpRange(ipStrStart, ipStrEnd, input.getSnmpCredit(), phaser, futureDeviceList);
            }
        }
        
        /* 指定IP列表 */
        if (!Utils.isEmpty(input.getIps())) {
            for (String ele : input.getIps()) {
                DeviceDiscoveryTask devDiscoverTask = new DeviceDiscoveryTask(ele, input.getSnmpCredit(), phaser);
                Future<DiscoveryDevice> futureDev = ProbeProvider.batchDevicesTaskSubmit(devDiscoverTask);
                if (futureDev != null) {
                    futureDeviceList.add(futureDev);
                }
            }
        }
        
        /* 等待所有IP地址探测、设备发现结束 */
        phaser.arriveAndAwaitAdvance();
        
        for (Future<DiscoveryDevice> rs : futureDeviceList) {
            try {
                if (rs.get() != null) {
                    deviceList.add(rs.get());    
                }
            } catch (Exception e) {
                LOG.error("get Discovery Result meet Exception");
                // TODO
            }
        }
        if (deviceList.size() > MAX_DEVICES_SIZE) {
            LOG.error("Discovery Devices is More than MaxThreshold!");
            // TODO
        }
        
        return deviceList;
    }
    
    /**
     * 
     * Description: 遍历指定起始和结束的IP范围
     *  
     * @author XXX<br>
     * @param startIp - String
     * @param endIp - String
     * @param credit - SnmpCredit
     * @param phaser - Phaser
     * @param futureDeviceList - List<Future<DiscoveryDevice>>
     * 
     */
    private void goThroughIpRange(String startIp, String endIp, SnmpCredit credit, Phaser phaser, 
            List<Future<DiscoveryDevice>> futureDeviceList) {
        long ipStart = 0;
        long ipEnd = 0;
        try {
            ipStart = IpUtils.getIP(InetAddress.getByName(startIp));
            ipEnd = IpUtils.getIP(InetAddress.getByName(endIp));
        } catch (UnknownHostException e1) {
           // TODO 异常处理
        }

        for (long ip = ipStart; ip <= ipEnd; ip++) {
            String strIp = null;
            try {
                strIp = IpUtils.toIP(ip).getHostAddress();
            } catch (UnknownHostException e) {
                //TODO 异常处理
            }
            DeviceDiscoveryTask devDiscoverTask = new DeviceDiscoveryTask(strIp, credit, phaser);
            Future<DiscoveryDevice> futureDev = ProbeProvider.batchDevicesTaskSubmit(devDiscoverTask);
            if (futureDev != null) {
                futureDeviceList.add(futureDev);
            }
        }
    }
    
    /**
     * 
     * Description: 获取 mibDictionaryMap
     *  
     * @author XXX<br>
     * @return <br>
     */
    public Map<String, String> getMibDictionaryMap() {
        return mibDictionaryMap;
    }
    
    /**
     * 
     * Description: getDiscoveryTasks 
     *  
     * @author XXX<br>
     * @return List
     */
    public List<DiscoveryTask> getDiscoveryTasks() {
        List<DiscoveryTask> tasks = Lists.newArrayList();
        tasks.addAll(getSingleDiscoveryTask().values());
        tasks.addAll(getPeriodicDiscoveryTask().values());
        return tasks;
    }
    
    /**
     * 
     * Description: 由DiscoveryInput构建DiscoveryTask
     *  
     * @author XXX<br>
     * @param input - DiscoveryInput
     * @return DiscoveryTask
     */
    public DiscoveryTask buildDiscoveryTask(DiscoveryInput input) {
        DiscoveryTask discoveryTask = new DiscoveryTask();
        if (input.getTaskId() != null) {
            discoveryTask.setTaskId(input.getTaskId());
        }
        if (input.getNetworkId() != null) {
            discoveryTask.setNetworkId(input.getNetworkId());
        }
        if (input.getProbeId() != null) {
            discoveryTask.setProbeId(input.getProbeId());
        }
        if (input.getTaskId() != null) {
            discoveryTask.setTaskId(input.getTaskId());
        }
        if (input.getDiscoveryType() != null) {
            discoveryTask.setDiscoveryType(input.getDiscoveryType());
        }
        if (input.getDiscoveryMethod() != null) {
            discoveryTask.setDiscoveryMethod(input.getDiscoveryMethod());
        }
        if (input.getDiscoveryCredit() != null) {
            discoveryTask.setDiscoveryCredit(input.getDiscoveryCredit());
        }
        if (input.getSnmpCredit() != null) {
            discoveryTask.setSnmpCredit(input.getSnmpCredit());
        }
        if (input.getKit() != null) {
            discoveryTask.setKit(input.getKit());
        }
        if (input.getIp() != null) {
            discoveryTask.setIp(input.getIp());
        }
        if (!Utils.isEmpty(input.getIps())) {
            discoveryTask.setIps(input.getIps());
        }
        if (!Utils.isEmpty(input.getCidrs())) {
            discoveryTask.setCidrs(input.getCidrs());
        }
        if (!Utils.isEmpty(input.getIpRanges())) {
            discoveryTask.setIpRanges(input.getIpRanges());
        }
        return discoveryTask;
    }
    
    public Map<String, DiscoveryTask> getSingleDiscoveryTask() {
        return singleDiscoveryTask;
    }

    public void setSingleDiscoveryTask(Map<String, DiscoveryTask> singleDiscoveryTask) {
        this.singleDiscoveryTask = singleDiscoveryTask;
    }

    public Map<String, DiscoveryTask> getPeriodicDiscoveryTask() {
        return periodicDiscoveryTask;
    }

    public void setPeriodicDiscoveryTask(Map<String, DiscoveryTask> periodicDiscoveryTask) {
        this.periodicDiscoveryTask = periodicDiscoveryTask;
    }

}
