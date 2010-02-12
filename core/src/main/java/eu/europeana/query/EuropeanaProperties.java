/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.query;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * This class fetches the europeana.properties files, however folks have decided to define
 * its whearabouts.  It checks for expected keys and refuses to instantiate if there are
 * missing properties.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EuropeanaProperties extends Properties {
    private Logger log = Logger.getLogger(getClass());

    public EuropeanaProperties() {
        String europeanaProperties = "";
        InputStream inputStream = null;
        try {
            europeanaProperties = System.getProperty("europeana.properties");
            if (europeanaProperties != null) {
                log.info("Found system property 'europeana.properties', resolved to " + new File(europeanaProperties).getCanonicalPath());
            }
            inputStream = getInputFromFile(europeanaProperties);
            if (inputStream == null) {
                log.info("System property 'europeana.properties' not found, checking environment.");
                europeanaProperties = System.getenv("EUROPEANA_PROPERTIES");
                if (europeanaProperties != null) {
                    log.info("Found env property 'EUROPEANA_PROPERTIES', resolved to " + new File(europeanaProperties).getCanonicalPath());
                }
                inputStream = getInputFromFile(europeanaProperties);
            }
            if (inputStream == null) {
                log.warn("No 'europeana.properties' found in system properties or environment, checking for legacy 'europeana.config'.");
                inputStream = getInputFromFile(System.getProperty("europeana.config"));
            }
            if (inputStream == null) {
                log.warn("No 'europeana.properties', checking for (test) resource.");
                inputStream = getClass().getResourceAsStream("/europeana-test.properties");
                log.info("Test 'europeana.properties' being used");
            }
        }
        catch (Exception e) {
            log.fatal("Error in resolving file defined with " + europeanaProperties);
            System.exit(1);
        }
        if (inputStream == null) {
            log.fatal(
                    "Configuration not available!\n" +
                            "Solutions:\n" +
                            "1) Start the JVM with parameter -Deuropeana.properties=/path/to/europeana.properties\n" +
                            "2) Set the environment variable 'EUROPEANA_PROPERTIES' to /path/to/europeana.properties"
            );
            System.exit(1);
        }
        try {
            load(inputStream);
        }
        catch (IOException e) {
            log.fatal("Unable to load 'europeana.properties' from input stream!");
            System.exit(1);
        }
        boolean complete = true;
        for (String expect : EXPECT) {
            String value = getProperty(expect);
            if (value == null) {
                log.warn("Missing property '" + expect + "'");
                complete = false;
            }
        }
        if (!complete) {
            log.fatal("Europeana configuration properties incomplete.  Check log of this class for warnings.");
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
            "db.driverClass",
            "db.jdbcUrl",
            "db.user",
            "db.password",
            "solr.baseUrl",
            "solr.timeout",
            "solr.retries",
            "solr.chunkSize",
            "smtp.host",
            "smtp.username",
            "smtp.password",
            "trigger.startDelay",
            "trigger.repeatInterval",
            "admin.to",
            "system.from",
            "feedback.to",
            "exception.to",
            "feedback.from",
            "cacheUrl",
            "resolverUrlPrefix",
            "displayPageUrl",
            "dashboard.normalized.import.repository",
            "dashboard.sandbox.import.repository",
            "cache.cacheRoot",
            "debug",
            "static.page.path",
            "message.resource",
            "message.cache.seconds"
    };
}