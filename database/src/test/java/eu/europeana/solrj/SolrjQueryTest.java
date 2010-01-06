package eu.europeana.solrj;

import eu.europeana.bootstrap.SolrStarter;
import eu.europeana.query.FacetType;
import eu.europeana.query.ResponseType;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 6, 2010 9:02:06 AM
 */

public class SolrjQueryTest {

    private static final String url = "http://localhost:8983/solr";
    /*
      CommonsHttpSolrServer is thread-safe and if you are using the following constructor,
      you *MUST* re-use the same instance for all requests.  If instances are created on
      the fly, it can cause a connection leak. The recommended practice is to keep a
      static instance of CommonsHttpSolrServer per solr server url and share it for all requests.
      See https://issues.apache.org/jira/browse/SOLR-861 for more details
    */

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

    @AfterClass
    public static void after() throws Exception {
        solrStarter.stop();
    }

    @Test
    public void testSolrjQuery() throws Exception {
        // Europeana Stuff
        ResponseType responseType = ResponseType.LARGE_BRIEF_DOC_WINDOW;


        // get server instance
        SolrServer server = getSolrServer();

        // create Solr Query
        SolrQuery query = new SolrQuery()
//                .setQueryType(QueryExpression.QueryType.SIMPLE_QUERY.toString()) // == "europeana"
                .setQuery("*:*")
                        // set paging stuff

                        // set facets
                .setFacet(true)
                .setFacetMinCount(0)
                .setFacetLimit(9)
//                .setFacetPrefix(FacetType.LANGUAGE.toString(), FacetType.LANGUAGE.getTagName()) // for each facet field
                .addFacetField(FacetType.LANGUAGE.toString())

                        // set filter constraints
                .setFilterQueries("LANGUAGE:mul")
                .setRows(responseType.getRows())
                .setStart(0); //  solr is zero based


        // get response from server

//        Thread.sleep(10000);
        QueryResponse rsp = server.query(query);

        // get result List

        SolrDocumentList docs = rsp.getResults();
        List<FacetField> list = rsp.getFacetFields();

        Assert.assertTrue("must return more then zero docs", docs.size() != 0);

        for (SolrDocument doc : docs) {
            System.out.println(doc.getFieldValue("dc_title"));
        }

        for (FacetField facetField : list) {
            List<FacetField.Count> values = facetField.getValues();
            for (FacetField.Count value : values) {
                System.out.println("get name + count" + value.getName() + value.getCount());
                System.out.println("get filter query" + value.getAsFilterQuery());
                System.out.println("get facet name" + value.getFacetField().getName());
            }
        }


    }

    @Test
    @Ignore
    public void testSolrjXmlUpload() throws Exception {
        throw new Exception("not implemented yet");
    }

    @Test
    @Ignore
    public void testSolrjBinaryUpload() throws Exception {
        CommonsHttpSolrServer solrServer = getSolrServer();

        // upload binary instead of XML
        solrServer.setRequestWriter(new BinaryRequestWriter());
        throw new Exception("not implemented yet");
    }


    private CommonsHttpSolrServer getSolrServer() {
        return server;
    }
}
