package eu.europeana;

import eu.europeana.bootstrap.StarterUtil;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Run the dashboard war in Jetty
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DashboardStarter {

	public static void main(String... args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new WebAppContext(StarterUtil.getEuropeanaPath() + "/dashboard/target/dashboard.war", "/dashboard"));
        server.start();
	}
}
