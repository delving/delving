package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.{BeforeAndAfterAll, Spec}
import _root_.org.scalatest.junit.JUnitRunner
import eu.europeana.core.BeanQueryModelFactory
import org.apache.solr.client.solrj.SolrQuery
import eu.europeana.core.querymodel.beans.FullBean


/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class BeanQueryModelFactorySpec extends Spec with BeforeAndAfterAll with ShouldMatchers with SolrTester {

  override def afterAll() = stopSolrServer

  val testCollId = "00101"
  val solrSever = getSolrSever
  loadDefaultData(solrSever, 14, testCollId)
  val queryModelFactory = new BeanQueryModelFactory

  describe("A SolrResponse that comes from the server") {

      describe("(when retrieving a FullDoc)") {
        val europeana_uri = "http://www.europeana.eu/resolve/record/00101/"
        val query = new SolrQuery("europeana_uri:\"" + europeana_uri + "\"")
        val response = solrServer.query(query)
        println (query)


        it("should return a valid FullDoc") {
          val fullBean : FullBean = queryModelFactory.getFullDocFromSolrResponse(response)
          fullBean.getEuropeanaUri should be (europeana_uri)
          response.getResults.getNumFound should be (1)
        }

      }

    }


//  Old test class
   /* private static final String url = "http://localhost:8983/solr";
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
    public void testCreateFromQueryParams() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("query", "*:*");
        request.addParameter("qf", new String[]{"LANGUAGE:mul"});
        request.addParameter("start", "1");
        request.addParameter("rows", "12");

        Map<String, String[]> requestMap = request.getParameterMap();
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
    }*/
}