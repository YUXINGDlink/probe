package com.dlink.dview8.probe.discovery.model;

import java.util.List;

import com.dlink.dview8.common.domain.model.discovery.DiscoveryDevice;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;

/**
 * 
 * <Description> DiscoveryTask继承 
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.discovery.model <br>
 */
public class DiscoveryTask extends DiscoveryInput {

    /**
     * serialVersionUID <br>
     */
    private static final long serialVersionUID = 1L;
    
    /**
     *  返回已发现的设备信息 
     */
    private List<DiscoveryDevice> devices;

    public DiscoveryTask() {
        
    }
    
    public DiscoveryTask(List<DiscoveryDevice> devices) {
        super();
        this.devices = devices;
    }
    
    public List<DiscoveryDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<DiscoveryDevice> devices) {
        this.devices = devices;
    }


    /**
     * Description: <br> 
     *  
     * @author XXX <br>
     * @return <br>
     */ 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((devices == null) ? 0 : devices.hashCode());
        return result;
    }

    /**
     * Description: <br> 
     *  
     * @author XXX <br>
     * @param obj
     * @return <br>
     */ 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscoveryTask other = (DiscoveryTask) obj;
        if (devices == null) {
            if (other.devices != null)
                return false;
        } else if (!devices.equals(other.devices))
            return false;
        return true;
    }

    /**
     * Description: <br> 
     *  
     * @author XXX <br>
     * @return <br>
     */ 
    @Override
    public String toString() {
        return "DiscoveryTask [" + (devices != null ? "devices=" + devices + ", " : "") + "getIpCount()=" + getIpCount()
                + ", " + (getTaskId() != null ? "getTaskId()=" + getTaskId() + ", " : "")
                + (getDiscoveryMethod() != null ? "getDiscoveryMethod()=" + getDiscoveryMethod() + ", " : "")
                + (getIp() != null ? "getIp()=" + getIp() + ", " : "")
                + (getIps() != null ? "getIps()=" + getIps() + ", " : "")
                + (getCidrs() != null ? "getCidrs()=" + getCidrs() + ", " : "")
                + (getDiscoveryType() != null ? "getDiscoveryType()=" + getDiscoveryType() + ", " : "")
                + (getDiscoveryCredit() != null ? "getDiscoveryCredit()=" + getDiscoveryCredit() + ", " : "")
                + (getSnmpCredit() != null ? "getSnmpCredit()=" + getSnmpCredit() + ", " : "")
                + (getKit() != null ? "getKit()=" + getKit() + ", " : "")
                + (getIpRanges() != null ? "getIpRanges()=" + getIpRanges() + ", " : "")
                + (getNetworkId() != null ? "getNetworkId()=" + getNetworkId() + ", " : "")
                + (getProbeId() != null ? "getProbeId()=" + getProbeId() + ", " : "")
                + (super.toString() != null ? "toString()=" + super.toString() + ", " : "")
                + (getClass() != null ? "getClass()=" + getClass() : "") + "]";
    }
}