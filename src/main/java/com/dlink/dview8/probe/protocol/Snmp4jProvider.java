package com.dlink.dview8.probe.protocol;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.dlink.dview8.common.domain.model.discovery.SnmpCredit;



/**
 * 
 * <Description> 基于Snmp4J实现的Snmp服务
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.protocol <br>
 */
public class Snmp4jProvider implements SnmpService {
    /**
     *  默认Snmp版本 
     */
    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    
    /**
     *  默认Snmp底层协议 
     */
    public static final String DEFAULT_PROTOCOL = "udp";
    
    /**
     *  默认Snmp端口号 
     */
    public static final int DEFAULT_PORT = 161;
    
    /**
     *  默认Snmp超时时间 
     */
    public static final long DEFAULT_TIMEOUT = 1000L;
    
    /**
     *  默认Snmp重试次数 
     */
    public static final int DEFAULT_RETRY = 0;
    
    /**
     *  默认Snmp Bulk 
     */
    private static final int MAX_REPETITIONS = 6;
    
    /**
     *  默认Snmp Bulk 
     */
    private static final int NON_REPEAERS = 0;
    
    /**
     * Snmp
     */
    private Snmp snmp = null;
    
    /**
     * 根据oid获取信息
     */
    @Override
    public String get(String ip, String oid, SnmpCredit credit) {
        // 设置 目标
        CommunityTarget target = createDefault(ip, credit.getReadCommunity(), credit.getVersion(),
                credit.getPort());
        Snmp snmp = null;
        String result = "";
        try {
            // PDU 对象
            PDU pdu = createPDU(target);
            pdu.add(new VariableBinding(new OID(oid)));

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            // 操作类型
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();
            // 解析Response
            if (response == null) {
                // result = "response is null";
            } else {
                if (response.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                    for (VariableBinding vb : vbs) {
                        if ("noSuchInstance".equals(vb.getVariable().toString())) {
                            result += "Error:" + vb + " ," + vb.getVariable().getSyntaxString();
                        } else {
                            result += vb;
                        }
                    }
                } else {
                    result += "Error:" + response.getErrorStatusText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
        return result;
    }

    @Override
    public String getNext(String ip, String oid, SnmpCredit credit) {

        CommunityTarget target = createDefault(ip, credit.getReadCommunity(), credit.getVersion(),
                credit.getPort());
        Snmp snmp = null;
        String result = "";
        try {
            // PDU pdu = new PDU();
            PDU pdu = createPDU(target);
            pdu.add(new VariableBinding(new OID(oid)));
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GETNEXT);
            ResponseEvent respEvent = snmp.send(pdu, target);
            System.out.println("PeerAddress:" + respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            if (response == null) {
                // result = "response is null";
            } else {
                if (response.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                    for (VariableBinding vb : vbs) {
                        result += vb + " ," + vb.getVariable().getSyntaxString();
                    }
                } else {
                    result += "Error:" + response.getErrorStatusText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
        return result;
    }

    @Override
    public String set(String ip, String oid, SnmpCredit credit) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String tableView(String ip, String oid, SnmpCredit credit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String walk(String ip, String oid, SnmpCredit credit) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * snmp4j getBulk功能
     * 
     * @param search
     * @return
     */
    @Override
    public String getBulk(String ip, String oid, SnmpCredit credit) {
        // 设置 目标
        CommunityTarget target = createDefault(ip, credit.getReadCommunity(), credit.getVersion(),
                credit.getPort());
        Snmp snmp = null;
        String result = "";
        try {
            // PDU 对象
            PDU pdu = createPDU(target);
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            // 操作类型
            pdu.setType(PDU.GETBULK);
            // snmp getBulk独有
            pdu.setMaxRepetitions(MAX_REPETITIONS); // 每个OID通过GETBULK方式获取多少个数据
            pdu.setNonRepeaters(NON_REPEAERS);

            pdu.add(new VariableBinding(new OID(oid)));
            ResponseEvent responseEvent = snmp.send(pdu, target);
            PDU response = responseEvent.getResponse();
            if (response == null) {

            } else {
                if (response.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                    for (VariableBinding vb : vbs) {
                        if ("noSuchInstance".equals(vb.getVariable().toString())) {
                            result += "Error:" + vb + " ," + vb.getVariable().getSyntaxString();
                        } else {
                            result += vb + "|";
                        }
                    }
                } else {
                    result += "Error:" + response.getErrorStatusText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
        return result;
    }

    @Override
    public String getList(String ip, String oid, SnmpCredit credit, List<String> oidList) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 
     * Description: 创建对象communityTarget
     *  
     * @author XXX<br>
     * @param ip - String
     * @param community - String
     * @param version - int
     * @param port - int
     * @return CommunityTarget
     */
    private CommunityTarget createDefault(String ip, String community, int version, int port) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip + "/" + port);
        Target target = null;

        if (version == (SnmpConstants.version3 + 1)) {
            version = SnmpConstants.version3;
            snmp.getUSM().addUser(new OctetString("MD5DES"), new UsmUser(new OctetString("MD5DES"), AuthMD5.ID,
                    new OctetString("MD5DESUserAuthPassword"), PrivDES.ID, new OctetString("MD5DESUserPrivPassword")));
            target = new UserTarget();
            // 设置安全级别
            ((UserTarget) target).setSecurityLevel(SecurityLevel.AUTH_PRIV);
            ((UserTarget) target).setSecurityName(new OctetString("MD5DES"));
            target.setVersion(SnmpConstants.version3);
        } else {
            target = new CommunityTarget();
            if (version == (SnmpConstants.version1 + 1)) {
                version = SnmpConstants.version1;
                target.setVersion(SnmpConstants.version1);
                ((CommunityTarget) target).setCommunity(new OctetString(community));
            } else {
                version = SnmpConstants.version2c;
                target.setVersion(SnmpConstants.version2c);
                ((CommunityTarget) target).setCommunity(new OctetString(community));
            }
        }
        target.setAddress(address);
        target.setVersion(version);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return (CommunityTarget) target;
    }

    /**
     * 创建PDU，snmp v1,v2,v3 ，pdu不同
     * 
     * @param target - Target
     * @return PDU
     */
    private PDU createPDU(Target target) {
        PDU request;
        if (target.getVersion() == 3) {
            request = new ScopedPDU();
            ScopedPDU scopedPDU = (ScopedPDU) request;
            OctetString contextEngineId = new OctetString(MPv3.createLocalEngineID());
            scopedPDU.setContextEngineID(contextEngineId);
            // scopedPDU.setContextName(this.contextName);//must be same as SNMP agent
        } else {
            request = new PDU();
        }
        return request;
    }

}
