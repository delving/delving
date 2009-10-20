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

package eu.europeana;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;


/**
 * Bootstrap the jetty server
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class PortalFullStarter {

    public Server startServer() throws Exception {
        return startServer(8080);
    }

    public Server startServer(int portNumber) throws Exception {
        File webappA = new File("./portal-full/src/main/webapp");
        File webappB = new File("./src/main/webapp");
        WebAppContext webAppContext;
        if (webappA.exists()) {
            webAppContext = new WebAppContext(webappA.toString(), "/portal");
        }
        else if (webappB.exists()) {
            webAppContext = new WebAppContext(webappB.toString(), "/portal");
        }
        else {
            throw new Exception("Unable to find the webapp dir. Please make sure you start from the root of " +
                    "the Europeana Project or from the root of the module");
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
