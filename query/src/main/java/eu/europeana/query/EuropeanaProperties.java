/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class fetches the europeana.properties files, however folks have decided to define
 * its whearabouts.  It checks for expected keys and refuses to instantiate if there are
 * missing properties.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EuropeanaProperties extends Properties {

    public EuropeanaProperties() {
        String configFile = System.getProperty("europeana.properties");
        Logger log = Logger.getLogger(getClass());
        if (configFile == null) {
            log.info("System property 'europeana.properties' not found, checking environment.");
            configFile = System.getenv("EUROPEANA_PROPERTIES");
            if (configFile != null) {
                log.info("Found environment variable EUROPEANA_PROPERTIES");
            }
            else { // todo
                System.out.println(System.getenv());
            }
        }
        if (configFile == null) {
            log.warn("No 'europeana.properties' found in system properties or environment, checking for legacy 'europeana.config'.");
            configFile = System.getProperty("europeana.config");
        }
        if (configFile == null) {
            log.fatal(
                    "Configuration not available!\n" +
                            "Solutions:\n" +
                            "1) Start the JVM with parameter -Deuropeana.properties=/path/to/europeana.properties\n" +
                            "2) Set the environment variable 'EUROPEANA_PROPERTIES' to /path/to/europeana.properties"
            );
            throw new RuntimeException("Configuration not available!");
        }
        log.info("Going to load properties from '"+configFile+"'");
        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            load(fileInputStream);
            log.info("Loaded properties from '"+configFile+"'");
        }
        catch (IOException e) {
            log.fatal("Unable to load 'europeana.properties' from [" + configFile + "]!");
            throw new RuntimeException("Unable to load 'europeana.properties' file!");
        }
        boolean complete = true;
        for (String expect : EXPECT) {
            String value = getProperty(expect); 
            if (value == null) {
                log.warn("Missing property '"+expect+"' in '"+configFile+"'");
                complete = false;
            }
        }
        if (!complete) {
            throw new IllegalStateException("Europeana configuration properties incomplete.  Check log for details.");
        }
    }

    private static String[] EXPECT = {
            "db.driverClass",
            "db.jdbcUrl",
            "db.user",
            "db.password",
            "solr.updateUrl",
            "solr.selectUrl",
            "solr.chunkSize",
            "piwik.enabled",
            "piwik.jsUrl",
            "piwik.logUrl",
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
            "template.path",
            "cacheUrl",
            "resolverUrlPrefix",
            "displayPageUrl",
            "dashboard.normalized.import.repository",
            "dashboard.sandbox.import.repository",
            "cache.imageMagickPath",
            "cache.cacheRoot",
            "debug",
            "message.source",
            "message.static_pages",
            "message.network",
    };
}