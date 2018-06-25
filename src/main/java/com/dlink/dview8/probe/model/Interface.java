package com.dlink.dview8.probe.model;

import java.util.UUID;

public class Interface {

	/* 接口所属网络 */
	private UUID networkId;
	
	/* 接口所属探测仪 */
	private UUID probeId;
	
	/* 接口所属设备 | 172.18.190.244 */
	private String deviceIp;
	
	/* 接口名称 | GigabitEthernet 1/1 */
	private String name;
	
	/* 接口描述 | D-Link DGS-3120-24TC R3.10.012 Port 1 */
	private String sysDescription;
	
	/* 接口类型 | GigabitEthernet */
	private String type;
	
	/* 接口IP地址 | 172.18.191.55 */
	private String ipAddr;
	
	/* 接口MAC地址 | 00:01:6C:06:A6:24 */
	private String macAddr;
	
	/* 4 User: 接口状态 | Up or Down */
	private String status;
	
	
	/* 带宽Rx | 10G */
	private String bandwidthRx;
	
	/* 带宽Tx | 10G */
	private String bandwidthTx;
	
	
	/* 总流量Rx |3G */
	private String totalOfTrafficRx;
	
	/* 总流量Rx |2G */
	private String totalOfTrafficTx;
	
	/* 当前（每秒）流量Rx | 30k */
	private String realTimeOfTrafficRx;
	
	/* 当前（每秒）流量Tx | 20k */
	private String realTimeOfTrafficTx;
	
	
	/* 接口利用率Rx | 30k */
	private String intfOfUtilizationRx;
	
	/* 接口利用率Tx | 20k */
	private String intfOfUtilizationTx;
	
	
	/* 报文累计总数Rx | 10000 */
	private String totalOfPacketsRx;
	
	/* 报文累计总数Tx | 10000 */
	private String totalOfPacketsTx;
	
	/* 当前（每秒）报文累计总数Rx | 30k */
	private String realTimeOfPacketsRx;
	
	/* 当前（每秒）报文累计总数Tx | 20k */
	private String realTimeOfPacketsTx;
	
	
	/* 单播报文累计总数Rx | 10000 */
	private String totalOfUcastRx;
	
	/* 单播报文累计总数Tx | 10000 */
	private String totalOfUcastTx;
	
	/* 多播报文累计总数Rx | 10000 */
	private String totalOfMulticastRx;
	
	/* 多播报文累计总数Tx | 10000 */
	private String totalOfMulticastTx;
	
	/* 广播报文累计总数Rx | 10000 */
	private String totalOfBroadcastRx;
	
	/* 广播报文累计总数Tx | 10000 */
	private String totalOfBroadcastTx;
	
	/* 丢报文累计总数Rx |10000 */
	private String totalOfDiscardsRx;
	
	/* 丢报文累计总数Tx | 10000 */
	private String totalOfDiscardsTx;
	
	/* 错报文累计总数Tx | 10000 */
	private String totalOfErrorsRx;
	
	/* 错报文累计总数Tx | 10000 */
	private String totalOfErrorsTx;
	
	
	
}
