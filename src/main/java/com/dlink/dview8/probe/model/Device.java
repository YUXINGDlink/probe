package com.dlink.dview8.probe.model;

import java.util.List;
import java.util.UUID;

public class Device {
	
	/* 设备所属网络 */
	private UUID networkId;
	
	/* 设备所属探测仪 */
	private UUID probeId;

	/* 设备IP地址 | 172.18.190.244 */
	private String ipAddr;
	
	/* 设备Mac地址 | 00:01:6C:06:A6:24 */
	private String macAddr;
	
	/* 设备发现方式 | ICMP、SNMP、ICMP+SNMP、CLI */
	private String discoveryType;
	
	/* 4 User: 设备监测方式 | ICMP or SNMP or RMON */
	private String monitorType;
	
	/* 4 User: 设备在线状态 | Online or Offline */
	private String status;
	
	
	/* 设备工作时长 | 196 hours 11 minutes 10 seconds */
	private String sysUpTime;
	
	/* 设备厂商 | D-LINK Systems */
	private String vendor;
	
	/* 具体类型 | L2 GE Switch */
	private String category;
	
	/* 设备类型 | Switch */
	private String deviceType;
	
	/* 设备型号 | DGS-3120-24TC */
	private String moduleType;
	
	/* 系统名称 | DGS-3120-24TC */
	private String sysName;
	
	/* 系统描述 | DGS-3120-24TC Gigabit Ethernet Switch */ 
	private String sysDescription;
	
	
	/* 4 User: 设备第一次被发现时间 | 10 May 2018 11:43:57 am CST */
	private String firstDiscoveredTime;
	
	/* 设备最后一次被发现时间 | 10 May 2018 12:43:57 am CST  */
	private String lastDiscoveredTime;
	
	/* 设备纳管理时间 | 4 User: 10 May 2018 11:43:57 am CST */
	private String managedTime;
	
	/* 设备配置协议 | snmp v1? v2? v3? */
	private String protocol;
	
	/* 设备序列号 | P4V01B5000013 */
	private String serialNumber;
	
	/* 设备端口总数 | 24 */
	private String portCount;
	
	/* 4 User: 设备当前任务 | task name */
	private List<String> task;
	
	/* 4 User: 设备LACP信息 | Lacp Info */
	private String lacpInfo;
	
	
	/* 4 User 用户新增设备注释 */
	private String site;  // 位置
	private String buildingNo;  // 楼号
	private String cabinet;  // 机柜
	private String contacts;
	private String department;  // 部门
	private String roomNo;  // 房号
	private String phone;  // 电话
	private String userSerialNumber;  // 用户定义设备序列号
	
	
}
