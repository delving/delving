package eu.europeana.json;

import eu.europeana.beans.BriefBean;
import eu.europeana.bootstrap.SolrStarter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.Assert.*;

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
        request.addParameter("qf", new String[]{"LANGUAGE:en", "LANGUAGE:de"});
        request.addParameter("start", "1");
        request.addParameter("rows", "12");

        Map<String,String[]> requestMap = request.getParameterMap();
        SolrQueryModelFactory queryModel = new SolrQueryModelFactory();
        SolrQuery solrQuery = queryModel.createFromQueryParams(requestMap);
        assertNotNull("solrQuery should not be null", solrQuery);
        assertEquals("query string should be equal", request.getParameter("query"), solrQuery.getQuery());
        assertEquals("Filter queries should be the same", request.getParameterValues("qf"), solrQuery.getFilterQueries());
    }

    @Test
    public void testGetSolrResponse() throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("LANGUAGE:mul");

        SolrQueryModelFactory queryModel = new SolrQueryModelFactory();
        queryModel.setSolrServer(server);
        QueryResponse queryResponse = queryModel.getSolrResponse(solrQuery, BriefBean.class);
        assertNotNull(queryResponse);
        assertTrue(queryResponse.getResults().getNumFound() > 0);
    }
}
