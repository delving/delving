package eu.europeana;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Run the dashboard war in Jetty
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DashboardStarter {

    public Server startServer() throws Exception {
        return startServer(8080);
    }

    public Server startServer(int portNumber) throws Exception {
        Server server = new Server(portNumber);
        server.addHandler(new WebAppContext("./dashboard/target/dashboard.war", "/dashboard"));
        server.start();
        return server;
    }

	public static void main(String... args) throws Exception {
		new DashboardStarter().startServer();
	}
}
