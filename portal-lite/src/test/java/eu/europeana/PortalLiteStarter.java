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
package eu.europeana;

import eu.europeana.core.util.StarterUtil;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Bootstrap the jetty server
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class PortalLiteStarter {

    public static void main(String... args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new WebAppContext(StarterUtil.getEuropeanaPath() + "/portal-lite/src/main/webapp", "/portal"));
        server.start();
    }
}
