package eu.europeana.solrj;

import eu.europeana.beans.BriefBean;
import eu.europeana.beans.FullBean;
import eu.europeana.beans.query.BeanQueryModelFactory;
import eu.europeana.beans.query.QueryType;
import eu.europeana.bootstrap.SolrStarter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 1:58:10 PM
 */

public class SolrQueryModelTest {

    private static final String url = "http://localhost:8983/solr";
    private static CommonsHttpSolrServer server;
    private static SolrStarter solrStarter;

    @BeforeClass
    public static void init() throws Exception {
        // start the solr server
        solrStarter = new SolrStarter();
        solrStarter.start();

        // connect to the solr server
        server = new CommonsHttpSolrServer(url);

        // server settings
        server.setSoTimeout(1000);  // socket read timeout
        server.setConnectionTimeout(100);
        server.setDefaultMaxConnectionsPerHost(100);
        server.setMaxTotalConnections(100);
        server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        server.setAllowCompression(true);
        server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
    }

    @Test
    public void testCreateFromQueryParams() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("query", "*:*");
        request.addParameter("qf", new String[]{"LANGUAGE:mul"});
        request.addParameter("start", "1");
        request.addParameter("rows", "12");

        Map<String,String[]> requestMap = request.getParameterMap();
        BeanQueryModelFactory solrQueryModelFactory = new BeanQueryModelFactory();
    }

    @Test
    public void testGetSolrResponse() throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("LANGUAGE:mul");


        BeanQueryModelFactory queryModel = new BeanQueryModelFactory();
        queryModel.setSolrServer(server);
        QueryResponse queryResponse = queryModel.getSolrResponse(solrQuery, BriefBean.class);
        assertNotNull(queryResponse);
        assertTrue(queryResponse.getResults().getNumFound() > 0);
    }

    @Test
    public void testGetFullSolrResponse() throws Exception {
        SolrQuery solrQuery = new SolrQuery("europeana_uri:\"http://www.europeana.eu/resolve/record/92001/79F2A36A85CE59D4343770F4A560EBDF5F207735\"");
        solrQuery.setQueryType(QueryType.MORE_LIKE_THIS_QUERY.toString());

        BeanQueryModelFactory queryModel = new BeanQueryModelFactory();
        queryModel.setSolrServer(server);
        QueryResponse queryResponse = queryModel.getSolrResponse(solrQuery);
        assertNotNull(queryResponse);
        assertTrue(queryResponse.getResults().getNumFound() > 0);

        List<BriefBean> list = queryResponse.getBeans(BriefBean.class);
        for (BriefBean briefBean : list) {
            assertNotNull(briefBean.getYear());
        }
        List<FullBean> fbList = queryResponse.getBeans(FullBean.class);
        for (FullBean fullBean : fbList) {
            assertNotNull(fullBean.getEuropeanaType());
            assertNotNull(fullBean.getEuropeanaCountry());
        }
        SolrDocumentList matchDoc = (SolrDocumentList) queryResponse.getResponse().get("match");
        List<FullBean> fullBean = server.getBinder().getBeans(FullBean.class, matchDoc);
        assertNotNull(fullBean);
    }
}
