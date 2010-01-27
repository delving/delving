package eu.europeana;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;


/**
 * Bootstrap the jetty server
 * @author Gerald de Jong <geralddejong@gmail.com>
 *
 */

@Deprecated // PortalLite can better be used because it also starts up the back-end services by itself
public class PortalLiteStarter {

    public Server startServer() throws Exception {
        return startServer(8080);
    }

    public Server startServer(int portNumber) throws Exception {
        File webappA = new File("./portal-lite/src/main/webapp");
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
		new PortalLiteStarter().startServer();
	}
}
