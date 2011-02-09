/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.delving.services;

import eu.europeana.core.util.StarterUtil;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */


public class MockServices {

    private static Server server = new Server(9999);

    public static void start() {
        try {
            String root = StarterUtil.getEuropeanaPath();
            System.setProperty("launch.properties", MockServices.class.getResource("/mock-launch.properties").getFile());
            System.setProperty("solr.solr.home", root + "/core/src/test/solr/single-core");
            System.setProperty("solr.data.dir", root + "/core/target/solrdata/mock_services");
            server.addHandler(new WebAppContext(root + "/services/src/main/webapp", "/services"));
            server.addHandler(new WebAppContext(root + "/core/src/test/solr/solr-1.4.1.war", "/solr"));
            server.start();
        }
        catch (Exception e) {
            throw new RuntimeException("Couldn't start server", e);
        }
    }

    public static void stop() {
        try {
            server.stop();
        }
        catch (Exception e) {
            throw new RuntimeException("Couldn't stop server", e);
        }
    }

    public static void main(String... args) throws InterruptedException {
        try {
            start();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Logger.getLogger(MockServices.class).info("Waiting 10 seconds, then kill again");
        Thread.sleep(10000);
        stop();
    }
}
