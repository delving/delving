package eu.europeana;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Run a Jetty container for testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated // is this still used???
public class DashboardTestServer {
	public static void main(String... args) throws Exception {
		WebAppContext webAppContext = new WebAppContext("./dashboard/target/dashboard", "/");
		Server server = new Server(8080);
		server.setHandler(webAppContext);
		server.start();
	}
}

