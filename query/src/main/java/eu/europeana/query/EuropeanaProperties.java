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

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class fetches the europeana.properties files, however folks have decided to define
 * its whearabouts.  It checks for expected keys, and installs all the properties as system
 * properties so they can be accessible from elsewhere.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EuropeanaProperties {
    private static final String LOADED_PROPERTY = "europeana.properties.loaded";
    private boolean complete = true;

    public EuropeanaProperties() {
        Logger log = Logger.getLogger(getClass());
        String europeanaPropertiesLoaded = System.getProperty(LOADED_PROPERTY);
        if (europeanaPropertiesLoaded != null) {
            log.info("Europeana properties already loaded into system properties in this JVM");
            return;
        }
        String configFile = System.getProperty("europeana.properties");
        if (configFile == null) {
            log.info("System property 'europeana.properties' not found, checking environment.");
            configFile = System.getenv("europeana.properties");
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
                            "2) Set the environment variable 'europeana.properties' to /path/to/europeana.properties"
            );
            throw new RuntimeException("Configuration not available!");
        }
        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            properties.load(fileInputStream);
        }
        catch (IOException e) {
            log.fatal("Unable to load 'europeana.properties' from [" + configFile + "]!");
            throw new RuntimeException("Unable to load 'europeana.properties' file!");
        }
        for (String expect : EXPECT) {
            String value = properties.getProperty(expect); 
            if (value == null) {
                log.warn("Missing property '"+expect+"' in '"+configFile+"'");
                complete = false;
            }
            else {
                log.info("System property '"+expect+"' set to '"+value+"'");
                System.setProperty(expect, value);
            }
        }
        System.setProperty(LOADED_PROPERTY, "true");
        log.info("System properties set from 'europeana.properties'.");
    }

    public void requireCompleteness() {
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