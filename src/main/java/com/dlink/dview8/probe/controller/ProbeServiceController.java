package com.dlink.dview8.probe.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dlink.dview8.common.api.Dview8Service;
import com.dlink.dview8.common.core.domain.result.Result;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryInput;
import com.dlink.dview8.common.domain.model.discovery.DiscoveryOutput;
import com.dlink.dview8.common.domain.model.discovery.SnmpCredit;
import com.dlink.dview8.probe.ProbeProvider;
import com.dlink.dview8.probe.model.DemoModel;
import com.dlink.dview8.probe.service.ProbeApiProvider;
import com.dlink.dview8.common.utils.Utils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <Description> <br> 
 *  
 * @author XXX <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe.controller <br>
 */
@RestController
@RequestMapping("/probe")
public class ProbeServiceController {

    /**
     * 
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProbeServiceController.class);
    
    /**
     *  Probe服务 
     */
    Dview8Service probeServie = new ProbeApiProvider();
    
    @RequestMapping(value = "/discovery", method = RequestMethod.POST)  
    public Result<DiscoveryOutput> addDiscovery(@RequestBody DiscoveryInput input) {
        Future<Result<DiscoveryOutput>> result = probeServie.discoverDevices(input, null);
        try {
            return result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Utils.failedWithValue(null);
    }
    
    @RequestMapping(value = "/snmpGet", method = RequestMethod.POST)  
    public Result<String> snmpGet(@RequestBody SnmpCredit input) {

        String value = ProbeProvider.getSnmpService().get("172.18.192.201", ".1.3.6.1.2.1.1.2.0", input);
        
        return Utils.success(value);
    }
    
    @RequestMapping(value = "/snmpGetBulk", method = RequestMethod.POST)  
    public Result<String> snmpGetBluk(@RequestBody SnmpCredit input) {

        String value = ProbeProvider.getSnmpService().getBulk("172.18.192.201", ".1.3.6.1.2.1.1", input);
        
        return Utils.success(value);
    }
    
    @RequestMapping("/hello")
    public String hello() {
        return "Hello World";
    }
    
    @RequestMapping(value = "/hello", method = RequestMethod.GET)  
    public String getHello() {  
        return "Hello, world.";  
    }
    
    @RequestMapping(value = "/hello", method = RequestMethod.POST)  
    public DemoModel postHello(@RequestBody DemoModel model) {
    	LOG.error("post ({}) successed", "hello");
    	System.out.println("model：" + model.getValue1());
        return model;  
    }
    
    @RequestMapping(value = "/hello", method = RequestMethod.PUT)  
    public Result<DemoModel> putHello(@RequestBody DemoModel model) {  
    	System.out.println("model：" + model.getValue1());
        return Utils.success(model);  
    }
    
    @RequestMapping(value = "/hello", method = RequestMethod.DELETE)  
    public Result<DemoModel> deleteHello(@RequestBody DemoModel model) {  
        return Utils.success(null);  
    }
}