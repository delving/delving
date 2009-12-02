package eu.europeana.controller.util;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class HostBasedContextLoaderListener extends ContextLoaderListener {
    private Logger log = Logger.getLogger(getClass());

    @Override
    protected ContextLoader createContextLoader() {
        return new ContextLoader() {
            @Override
            protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
                String [] configLocations = applicationContext.getConfigLocations();
                String [] embellished = new String[configLocations.length+1];
                embellished[0] = "classpath:"+getExistingHostContextFile();
                System.arraycopy(configLocations, 0, embellished, 1, configLocations.length);
                applicationContext.setConfigLocations(embellished);
            }
        };
    }

    private String getExistingHostContextFile() {
        String hostContextFile = getHostContextFile();
        log.info("checking for /"+hostContextFile);
        URL resource = getClass().getResource("/"+hostContextFile);
        if (resource == null) {
            log.warn("/"+hostContextFile+" not found");
            hostContextFile = "host-application-context.xml";
        }
        log.info("using /"+hostContextFile);
        return hostContextFile;
    }

    private String getHostContextFile() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            host = "host";
        }
        return host+"-application-context.xml";
    }

}
