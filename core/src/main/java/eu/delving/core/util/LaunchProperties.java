package eu.delving.core.util;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * This class fetches the properties files, however folks have decided to define
 * its whearabouts.  It checks for expected keys and refuses to instantiate if there are
 * missing properties.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class LaunchProperties extends Properties {

    public LaunchProperties(List<String> expectedProperties) {
        Logger log = Logger.getLogger(getClass());
        String propertyFilePath = System.getProperty("launch.properties");
        if (propertyFilePath == null) {
            log.fatal("Property file path must be defined with -Dlaunch.properties=/path/to/property/file");
            System.exit(1);
        }
        try {
            InputStream inputStream = new FileInputStream(propertyFilePath);
            load(inputStream);
            boolean complete = true;
            for (String expect : expectedProperties) {
                String value = getProperty(expect);
                if (value == null) {
                    log.warn(MessageFormat.format("Missing property ''{0}''", expect));
                    complete = false;
                }
            }
            if (!complete) {
                System.exit(1);
            }
        }
        catch (Exception e) {
            log.fatal("Error loading properties from " + propertyFilePath);
            System.exit(1);
        }
    }
}