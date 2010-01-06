package eu.europeana.solrj;

import eu.europeana.bootstrap.SolrStarter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 6, 2010 9:02:06 AM
 */

public class SolrjBeanTest {

    private final String url = "http://localhost:8983/solr";
    /*
      CommonsHttpSolrServer is thread-safe and if you are using the following constructor,
      you *MUST* re-use the same instance for all requests.  If instances are created on
      the fly, it can cause a connection leak. The recommended practice is to keep a
      static instance of CommonsHttpSolrServer per solr server url and share it for all requests.
      See https://issues.apache.org/jira/browse/SOLR-861 for more details
    */

    private static CommonsHttpSolrServer server;

    @BeforeClass
    public void init() throws Exception {
        // start the solr server
        SolrStarter solrStarter = new SolrStarter();
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
    public void testSolrjAddBean() throws Exception {

    }

    @Test
    @Ignore
    public void testSolrjGetBeans() throws Exception {
        // get server instance
        SolrServer server = getSolrServer();

        // create Solr Query
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");

        // get response from server

        QueryResponse rsp = server.query(query);

        // get beans todo replace Item with Europeana Bean class

//         List<Item> beans = rsp.getBeans(Item.class);

    }

    @Test
    @Ignore
    public void testSolrjQueryBea() throws Exception {
        throw new Exception("not implemented yet");
    }


    private CommonsHttpSolrServer getSolrServer() {
        return server;
    }
}