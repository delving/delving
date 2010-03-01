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

import eu.europeana.bootstrap.SolrStarter;
import eu.europeana.core.util.StarterUtil;

/**
 * Bootstrap the backend and portal lite including the annotation services
 *
 * @author Christian Sadilek
 */
public class PortalLiteWithAnnotation {

	// do not forget to set -Deuropeana.properties and working directory -> see readme file
    public static void main(String... args) throws Exception {
    	System.setProperty("hibernate.bytecode.provider", "javassist");
        
    	new SolrStarter().start();
    	
    	String aitRoot = StarterUtil.getAITPath();
        Server server = new Server(8080);
        server.addHandler(new WebAppContext(StarterUtil.getEuropeanaPath() + "/portal-lite/src/main/webapp", "/portal"));
        server.addHandler(new WebAppContext(aitRoot + "/annotation-middleware/target/annotation-middleware.war", "/annotation-middleware"));
        server.addHandler(new WebAppContext(aitRoot + "/image-annotation-frontend/target/image-annotation-frontend.war", "/image-annotation-frontend"));
        server.start();

    }
}