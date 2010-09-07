package eu.delving.services;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * * This class fetches the meta-repo.properties files, however folks have decided to define
 * its whearabouts.  It checks for expected keys and refuses to instantiate if there are
 * missing properties.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 25, 2010 11:23:19 PM
 */
public class MetaRepoProperties extends Properties {
    private Logger log = Logger.getLogger(getClass());
    private static final long serialVersionUID = -3746498853663716070L;

    public MetaRepoProperties() {
        String metaRepoPropretiesFile = "";
        InputStream inputStream = null;
        try {
            metaRepoPropretiesFile = System.getProperty("meta-repo.properties");
            if (metaRepoPropretiesFile != null) {
                log.info("Found system property 'meta-repo.properties', resolved to " + new File(metaRepoPropretiesFile).getCanonicalPath());
            }
            inputStream = getInputFromFile(metaRepoPropretiesFile);
            if (inputStream == null) {
                log.info("System property 'meta-repo.properties' not found, checking environment for META_REPO_PROPERTIES.");
                metaRepoPropretiesFile = System.getenv("META_REPO_PROPERTIES");
                if (metaRepoPropretiesFile != null) {
                    log.info("Found env property 'META_REPO_PROPERTIES', resolved to " + new File(metaRepoPropretiesFile).getCanonicalPath());
                }
                inputStream = getInputFromFile(metaRepoPropretiesFile);
            }
        }
        catch (Exception e) {
            log.fatal("Error in resolving file defined with " + metaRepoPropretiesFile);
            System.exit(1);
        }
        if (inputStream == null) {
            log.fatal(
                    "Configuration not available!\n" +
                            "Solutions:\n" +
                            "1) Start the JVM with parameter -Dmeta-repo.properties=/path/to/meta-repo.properties\n" +
                            "2) Set the environment variable 'META_REPO_PROPERTIES' to /path/to/meta-repo.properties"
            );
            System.exit(1);
        }
        try {
            load(inputStream);
        }
        catch (IOException e) {
            log.fatal("Unable to load 'meta-repo.properties' from input stream!");
            System.exit(1);
        }
        boolean complete = true;
        for (String expect : EXPECT) {
            String value = getProperty(expect);
            if (value == null) {
                log.warn(MessageFormat.format("Missing property ''{0}''", expect));
                complete = false;
            }
        }
        if (!complete) {
            log.fatal("MetaRepo configuration properties incomplete.  Check log of this class for warnings.");
            System.exit(1);
        }
    }

    private InputStream getInputFromFile(String filePath) {
        if (filePath != null) {
            try {
                log.info("Going to load properties from '" + filePath + "', resolved to " + new File(filePath).getCanonicalPath());
                return new FileInputStream(filePath);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException("No file found: " + filePath, e);
            }
            catch (IOException e) {
                throw new RuntimeException("IO exception on: " + filePath, e);
            }
        }
        else {
            return null;
        }
    }

    private static String[] EXPECT = {
            "repositoryName",
            "adminEmail",
            "earliestDateStamp",
            "repositoryIdentifier",
            "sampleIdentifier",
            "responseListSize"
    };
}
