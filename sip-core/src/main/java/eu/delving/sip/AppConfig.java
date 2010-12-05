/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.sip;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


@XStreamAlias("sip-creator-configuration")
public class AppConfig {
    private String serverHost;
    private String accessKey;
    private String recentDirectory;
    private String normalizeDirectory;

    public String getServerHost() {
        if (serverHost == null) {
            serverHost = "localhost";
        }
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getAccessKey() {
        if (accessKey == null) {
            accessKey = "";
        }
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getRecentDirectory() {
        if (recentDirectory == null) {
            recentDirectory = System.getProperty("user.home");
        }
        return recentDirectory;
    }

    public void setRecentDirectory(String directory) {
        this.recentDirectory = directory;
    }

    public String getNormalizeDirectory() {
        if (normalizeDirectory == null) {
            normalizeDirectory = System.getProperty("user.home");
        }
        return normalizeDirectory;
    }

    public void setNormalizeDirectory(String directory) {
        this.normalizeDirectory = directory;
    }
}
