package eu.delving.services.controller;

import eu.delving.core.rest.DataSetInfo;
import eu.delving.services.util.MongoDaemonRunner;
import eu.delving.services.util.ZipUploader;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * Go through the whole cycle with test data
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-application-context.xml")
public class TestDatasetController {
    private static final String URL = "http://localhost:8983/services/dataset";
    private static final Logger LOG = Logger.getLogger(TestDatasetController.class);
    private static MongoDaemonRunner daemonRunner;
    private static Server server;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeClass
    public static void startUp() {
        try {
            LOG.info("Starting mongo daemon");
            daemonRunner = new MongoDaemonRunner();
            daemonRunner.start();
            daemonRunner.waitUntilRunning();
            LOG.info("Launching web application");
            server = new Server(8983);
            server.setHandler(new WebAppContext("services/src/main/webapp", "/services"));
            server.start();
            LOG.info("Uploading test collection");
            ZipUploader zipUploader = new ZipUploader();
            zipUploader.upload();
            LOG.info("Collection ready");
        }
        catch (Exception e) {
            LOG.fatal("Problem starting up", e);
            System.exit(1);
        }
    }

    @AfterClass
    public static void stop() throws Exception {
        daemonRunner.kill();
        server.stop();
    }

    // todo: not a test yet with no assertions
    @Test
    public void fetchList() throws IOException {
        LOG.info("fetching list " + URL);
        List list = restTemplate.getForObject(URL, List.class);
        for (Object obj : list) {
            DataSetInfo info = (DataSetInfo) obj;
            LOG.info("dataset info received " + info.spec);
        }
    }

    // todo: not a test yet with no assertions
    @Test
    public void enable() throws IOException {
        DataSetInfo dataSet = restTemplate.getForObject(URL+"/indexing/00101_Ag_NO_sffDF?enable=true", DataSetInfo.class);
        LOG.info("enabled? "+dataSet.spec);
    }

}
