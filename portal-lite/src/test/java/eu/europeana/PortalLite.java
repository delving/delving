/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or? as soon they
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
import java.io.FileFilter;

/**
 * This class uses a jetty web server to fire up the Europeana back-end services (api, and solr) and
 * then the portal.
 *
 * NOTE: it requires that the europeana.properties file points to port 8080 for these services:
 *
 * solr.baseUrl = http://localhost:8080/solr
 * cacheUrl = http://localhost:8080/api/image/?
 * resolverUrlPrefix  = http://localhost:8080/api/resolve
 * displayPageUrl     = http://localhost:8080/portal/full-doc.html
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Deprecated
public class PortalLite {

    public static void main(String... args) throws Exception {
        String root = "./";
        if (checkDirectory(new File("."))) {
            root = "./";
        }
        else if (checkDirectory(new File("../"))) {
            root = "../";
        }
        else {
            System.out.println("This bootstrap class must be started with home directory 'europeana'");
            System.exit(1);
        }
        System.setProperty("solr.solr.home", root + "core/src/test/solr/home");
        Server server = new Server(8080);
        server.addHandler(new WebAppContext(root + "api/src/main/webapp", "/api"));
        server.addHandler(new WebAppContext(root + "core/src/test/solr/solr.war", "/solr"));
        server.addHandler(new WebAppContext(root + "portal-lite/src/main/webapp", "/portal"));
        server.start();
    }

    private static boolean checkDirectory(File here) {
        File[] subdirs = here.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        return checkFor("portal-lite", subdirs)
                && checkFor("api", subdirs)
                && checkFor("core", subdirs);
    }

    private static boolean checkFor(String name, File[] subdirs) {
        for (File subdir : subdirs) {
            if (subdir.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}