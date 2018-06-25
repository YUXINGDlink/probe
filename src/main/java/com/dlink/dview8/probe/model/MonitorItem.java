package com.dlink.dview8.probe.model;

import java.util.UUID;

public class MonitorItem {
	
	/* 设备所属网络 */
	private UUID networkId;
	
	/* 设备所属探测仪 */
	private UUID probeId;
	
	/* 设备所属设备 | 172.18.190.244 */
	private String deviceIp;
	
	/* CPU利用率 | 20% */
	private String cpuOfUtilization;
	
	/* 磁盘利用率 | 20% */
	private String diskOfUtilization;
	
	/* 已用磁盘空间 | 20G */
	private String usedDiskSpace;
	
	/* 磁盘空间 | 200G */
	private String diskSpace;
	
	/* 内存利用率 | 20% */
	private String memoryOfUtilization;
	
	/* 已用内存空间 | 2G */
	private String usedMemorySpace;
	
	/* 内存空间 | 8G */
	private String memorySpace;
	
	/* 进程数 | 100 */
	private String processCount;
	
	/* 是否开启Trap | Up or Down */
	private String trap;
	
	/* 是否开启Syslog | Up or Down */
	private String syslog;
	
	/* 接口监视项 | intf info */
	private String intfMonitor;
}
