package eu.europeana.core.querymodel.query

import _root_.org.apache.solr.client.solrj.SolrQuery
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.{PrivateMethodTester, Spec}
import _root_.org.springframework.mock.web.MockHttpServletRequest

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class DocIdWindowPagerSpec extends Spec with ShouldMatchers with PrivateMethodTester {

/* methods to tested
 *
 * - fetchParameter
 * - setQueryStringForPaging
 * - setReturnToResults
 * - setNextAndPrevious
 * - getSolrStart
 * - getFullDocInt
 */

  describe("A DocIdWindowPager") {

    describe("(when getting a parameter from the ParameterMap)") {
      val secret = PrivateMethod[String]('setQueryStringForPaging)
      val request = new MockHttpServletRequest
      val parameters = List("p1" -> "v1", "p2" -> "v2")
      parameters foreach (param => request setParameter (param._1, param._2))

      it("should give back the formatted query string") {
        val query = new SolrQuery("sjoerd")
        val pager = new DocIdWindowPagerImpl
        pager invokePrivate secret(query, "1")
        pager.getQueryStringForPaging should equal ("query=sjoerd&startPage=1")
      }


    }

  }

  /*
  private Logger log = Logger.getLogger(TestDocIdWindowPager.class);

    private MockHttpServletRequest request;

    @Autowired
    private IngestionFixture ingestionFixture;

    @Autowired
    private DashboardDao dashboardDao;

    private static EuropeanaCollection collection;
    private static ImportFile importFile;
    private static SolrServer solrServer; // todo
    private Class<? extends DocId> idBean = IdBean.class;

    @Before
    public void init() throws Exception {
        if (collection == null) {
            ingestionFixture.deleteImportRepository();
            log.info("=== initializing test ingestion ===");
            List<ImportFile> importFiles = ingestionFixture.getImportRepository().getAllFiles();
            Assert.assertEquals(1, importFiles.size());
            importFile = importFiles.get(0);
            log.info(importFile);
            collection = dashboardDao.fetchCollection(importFile.deriveCollectionName(), importFile.getFileName(), true);
            log.info(collection.getName());
            ingestionFixture.startSolr();
        }
    }

    @After
    public void cleanup() throws Exception {
//        ingestionFixture.stopSolr();
    }

//    @Test
    public void hasNextAndPreviousTest() throws SolrServerException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/74108B5CC7D4A0B86C7C5E53EC8300F17ED9AF2D";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "2");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer, idBean);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getNextInt(), 3);
        assertEquals(pager.getPreviousInt(), 1);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        System.out.println(pager.toString());
    }


//    @Test
    public void hasNoPrevious() throws SolrServerException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "1");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer, idBean);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getNextInt(), 2);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

//    @Test
    public void hasNoNext() throws SolrServerException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "13");
        request.addParameter("start", "14");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer, idBean);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.getPreviousInt(), 13);
        assertEquals(pager.getDocIdWindow().getIds().size(), 2);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

//    @Test
    public void arrayOutOfBounds() throws SolrServerException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("start", "18");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer, idBean);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 0);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }

//    @Test
    public void returnToBob() throws SolrServerException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("start", "1");
        request.addParameter("query", "1920 max devrient");
        request.addParameter("pageId", "yg");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer, idBean);
        assertEquals(pager.getReturnToResults(), "year-grid.html?query=max+devrient&bq=1920+max+devrient&start=1&view=table&tab=null");
        assertEquals(pager.isNext(), true);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }
   */
}