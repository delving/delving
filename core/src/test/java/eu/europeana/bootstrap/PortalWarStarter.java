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

package eu.europeana.bootstrap;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Convenience class to test functionality of build portal.war file
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class PortalWarStarter {
    private Server server;

    public void start() throws Exception {
        // todo: add test to see if portal.war is actually build
        WebAppContext webAppContext = new WebAppContext("./portal-lite/target/portal.war", "/portal");
        server = new Server(8080);
        server.setHandler(webAppContext);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String... args) throws Exception {
        new PortalWarStarter().start();
    }

}