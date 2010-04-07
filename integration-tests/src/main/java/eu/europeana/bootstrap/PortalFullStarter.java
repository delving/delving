/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.bootstrap;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import eu.europeana.core.util.StarterUtil;

import java.io.File;


/**
 * Bootstrap the jetty server
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class PortalFullStarter {

    public static final String PORTAL_PATH = "/portal";

	public static final int PORT = 8081;

    public static final String PORTAL_URL = "http://localhost:" + PORT + PORTAL_PATH;
    
    
	public Server startServer() throws Exception {
        return startServer(PORT);
    }

    public Server startServer(int portNumber) throws Exception {

        File webappA = new File(StarterUtil.getEuropeanaPath() + "/portal-full/src/main/webapp");
         WebAppContext webAppContext;
        if (webappA.exists()) {
            webAppContext = new WebAppContext(StarterUtil.getEuropeanaPath() + "/portal-full/src/main/webapp", PORTAL_PATH);
            webAppContext.setExtraClasspath("./portal-full/target/classes/;./portal-full/src/main/resources/;./integration-tests/src/main/resources/;./api/src/main/resources/;./core/src/test/resources/");
        }
        else {
            throw new Exception("Unable to find the webapp dir. Please make sure you start from the root of " +
                    "the Europeana Project ");
        }
        Server server = new Server(portNumber);
        server.setHandler(webAppContext);
        server.start();
        return server;
    }

    public static void main(String... args) throws Exception {
        new PortalFullStarter().startServer();
    }
}
