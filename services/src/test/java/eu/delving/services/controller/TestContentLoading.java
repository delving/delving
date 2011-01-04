package eu.delving.services.controller;

import eu.delving.services.core.Harvindexer;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.util.MockContentLoader;
import eu.delving.sip.DataSetState;
import eu.europeana.core.util.StarterUtil;
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

/**
 * Go through the whole cycle with test data
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/services-application-context.xml",
        "/core-application-context.xml"
})
public class TestContentLoading {
    private static final String URL = "http://localhost:8983/services/dataset";
    private static final Logger LOG = Logger.getLogger(TestContentLoading.class);
    private static Server server;

    @Autowired
    private MetaRepo metaRepo;

    @Autowired
    private Harvindexer harvindexer;

    @BeforeClass
    public static void startUp() {
        try {
            LOG.info("Starting mongo daemon");
            String root = StarterUtil.getEuropeanaPath();
            System.setProperty("solr.solr.home", root + "/core/src/test/solr/home");
            if (System.getProperty("solr.data.dir") == null) {
                System.setProperty("solr.data.dir", root + "/core/target/solrdata");
            }
            LOG.info("Launching web application");
            server = new Server(8983);
            server.setHandler(new WebAppContext(root + "/services/src/main/webapp", "/services"));
            server.addHandler(new WebAppContext(root + "/core/src/test/solr/solr-1.4.1.war", "/solr"));
            server.start();
            LOG.info("Uploading test collection");
            MockContentLoader contentLoader = new MockContentLoader("CW-D42F6D333E5FA210D64C");
            contentLoader.run();
            LOG.info("Collection ready");
        }
        catch (Exception e) {
            LOG.fatal("Problem starting up", e);
            System.exit(1);
        }
    }

    @AfterClass
    public static void stop() throws Exception {
        server.stop();
    }

    @Test
    public void testImporting() throws Exception {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet("92001_Ag_EU_TELtreasures");
        dataSet.setState(DataSetState.INDEXING);
        dataSet.save();
        harvindexer.commenceImport(dataSet);
        while (!harvindexer.getActiveImports().isEmpty()) {
            Thread.sleep(500);
        }
        harvindexer.commitSolr();
    }

}
