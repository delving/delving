package eu.delving.services.indexing;

import eu.delving.services.core.MetaRepo;
import eu.europeana.core.database.ConsoleDao;
import eu.europeana.core.database.domain.EuropeanaCollection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 28, 2010 11:52:39 AM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/services-application-context.xml","/core-application-context.xml"})
public class TestHarvindexer {

    @Autowired
    private Harvindexer harvindexer;

    @Autowired
    private ConsoleDao consoleDao;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private MetaRepo metaRepo;

    @Value("#{launchProperties['services.url']}")
    private String servicesUrl;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRetrievePmhRequest() throws Exception {
        HttpMethod method = new GetMethod(String.format("%s/oai-pmh?verb=ListRecords&metadataPrefix=ese&set=%s", servicesUrl, "92001_Ag_EU_TELtreasures"));
        httpClient.executeMethod(method);
        InputStream inputStream = method.getResponseBodyAsStream();
    }

    @Test
    public void testCommenceImport() throws Exception {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet("92001_Ag_EU_TELtreasures");
        dataSet.setState(MetaRepo.DataSetState.INDEXING);
        dataSet.save();
        EuropeanaCollection collection = consoleDao.fetchCollection("92001_Ag_EU_TELtreasures", "92001_Ag_EU_TELtreasures.xml", false);
        harvindexer.commenceImport(collection.getId());
        while (!harvindexer.getActiveImports().isEmpty()) {
            Thread.sleep(500);
        }
    }
}
