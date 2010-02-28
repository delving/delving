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

import java.io.File;
import java.io.FileFilter;

/**
 * Bootstrap the entire system, including the ApacheSolr, resolver and cache servlet
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated // PortalFull can be run now, which does this as well
public class EuropeanaBackendStarter {

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
        int port = 8983;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.addHandler(new WebAppContext(root + "api/src/main/webapp", "/api"));
        server.addHandler(new WebAppContext(root + "core/src/test/solr/solr.war", "/solr"));
        server.start();
    }

    private static boolean checkDirectory(File here) {
        File[] subdirs = here.listFiles(new FileFilter() {
            @Override
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
