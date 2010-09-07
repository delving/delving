package eu.delving.services.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Fire up the meta repo for testing
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoStarter {

    public static void main(String... args) throws Exception {
        final MongoDaemonRunner runner = new MongoDaemonRunner();
        runner.start();
        runner.waitUntilRunning();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("executing shutdown hook");
                runner.kill();
            }
        });
        Server server = new Server(8080);
        server.setHandler(new WebAppContext("meta-repo/src/main/webapp", "/meta-repo"));
        server.start();
    }

}
