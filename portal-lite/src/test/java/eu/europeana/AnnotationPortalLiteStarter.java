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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Bootstrap the backend and portal lite including the annotation services
 *
 * @author Christian Sadilek
 */
public class AnnotationPortalLiteStarter {

	// do not forget to set -Deuropeana.properties and working directory -> see readme file
    public static void main(String... args) throws Exception {
    	System.setProperty("solr.solr.home", "./bootstrap/src/test/solr/solr");
    	System.setProperty("hibernate.bytecode.provider", "javassist");
        
    	int backendPort = 8983;
    	int frontendPort = 8080;

    	Server serverBackEnd = new Server(backendPort);
    	serverBackEnd.addHandler(new WebAppContext("api/src/main/webapp", "/api"));
    	serverBackEnd.addHandler(new WebAppContext("bootstrap/src/test/solr/apache-solr-1.4-dev.war", "/solr"));
    	serverBackEnd.start();
    	
    	Server serverFrontEnd = new Server(frontendPort);
    	serverFrontEnd.addHandler(new WebAppContext("portal-lite/src/main/webapp", "/portal"));        
        // TODO fix the paths
    	serverFrontEnd.addHandler(new WebAppContext("../annotation-middleware/target/annotation-middleware", "/annotation-middleware"));
    	serverFrontEnd.addHandler(new WebAppContext("../image-annotation-frontend/target/image-annotation-frontend", "/image-annotation-frontend"));
    	serverFrontEnd.start();
    }
}