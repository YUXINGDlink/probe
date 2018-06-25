package com.dlink.dview8.probe;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * <Description> <br> 
 *  
 * @author Sun Tain Yu <br>
 * @version 1.0 <br>
 * @CreateDate 2018年6月8日 <br>
 * @since V1.0 <br>
 * @see com.dview8.probe <br>
 */
@SpringBootApplication
public class ProbeApplication implements ApplicationRunner {

    /**
     * 
     * Description: Probe Main
     *  
     * @author XXX<br>
     * @param args <br>
     */
    public static void main(String[] args) {
        SpringApplication.run(ProbeApplication.class, args);
    }

    /**
     * 
     * Description: ApplicationRunner
     *  
     * @author XXX <br>
     * @param args
     * @throws Exception <br>
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ProbeProvider probe = ProbeProvider.createInstance();
        probe.start();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
