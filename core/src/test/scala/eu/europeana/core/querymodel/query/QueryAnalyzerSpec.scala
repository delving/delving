package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class QueryAnalyzerSpec extends Spec with ShouldMatchers {

  /*
  * private static QueryAnalyzer qa;

    @BeforeClass
    public static void init() {
        qa = new QueryAnalyzer();
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(list);
        qa.setAnnotationProcessor(annotationProcessor);
    }

    @Test
    public void variety() throws Exception {
        simple("very simple query");
        advanced("advanced and Query");
        advanced("text:fieldedQuery");
        advanced("+\"phrase query\"");
        moreLike("europeana_uri:\"http://www.europeana.eu/bla/bla\"");
    }

    @Test
    public void sanitize() {
        sanitize("hello { and { goodbye }", "hello AND goodbye");
    }

    @Test(expected = EuropeanaQueryException.class)
    public void testIllegalSearchField() throws Exception {
        advanced("dc_title:dimitry");
        advanced("gumby:goes_bad");
    }

    @Test
    public void testCreateAdvancedQuery() throws Exception {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("facet1", new String[]{""});
        params.put("query1", new String[]{"treasure"});
        params.put("operator2", new String[]{""});
        params.put("facet2", new String[]{""});
        params.put("query2", new String[]{""});
        params.put("operator3", new String[]{""});
        params.put("facet3", new String[]{""});
        params.put("query3", new String[]{""});
        String advancedQueryString = qa.createAdvancedQuery(params);
        assertNotNull(advancedQueryString);
        assertEquals("queries should be equal",
                "text:treasure",
                advancedQueryString);

        Map<String, String[]> paramsAllFacets = new HashMap<String, String[]>();
        paramsAllFacets.put("facet1", new String[]{"text"});
        paramsAllFacets.put("query1", new String[]{"treasure"});
        paramsAllFacets.put("operator2", new String[]{"OR"});
        paramsAllFacets.put("facet2", new String[]{"title"});
        paramsAllFacets.put("query2", new String[]{"chest"});
        paramsAllFacets.put("operator3", new String[]{"OR"});
        paramsAllFacets.put("facet3", new String[]{"author"});
        paramsAllFacets.put("query3", new String[]{"max"});
        advancedQueryString = qa.createAdvancedQuery(paramsAllFacets);
        assertNotNull(advancedQueryString);
        assertEquals("queries should be equal",
                "text:treasure OR title:chest OR author:max",
                advancedQueryString);
    }

    private void sanitize(String from, String to) {
        assertEquals("Not sanitized properly", to, qa.sanitize(from));
    }

    private void simple(String query) throws EuropeanaQueryException {
        assertEquals("Not a simple query [" + query + "]", QueryType.SIMPLE_QUERY, qa.findSolrQueryType(query));
    }

    private void advanced(String query) throws EuropeanaQueryException {
        assertEquals("Not an advanced query [" + query + "]", QueryType.ADVANCED_QUERY, qa.findSolrQueryType(query));
    }

    private void moreLike(String query) throws EuropeanaQueryException {
        assertEquals("Not a more-like query [" + query + "]", QueryType.MORE_LIKE_THIS_QUERY, qa.findSolrQueryType(query));
    }


    @Test
    public void testCreateRefineSearch() throws Exception {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("query", new String[]{"ioan"});
        params.put("rq", new String[]{"mercury"});
        String refinedQueryString = qa.createRefineSearchFilterQuery(params);
        assertNotNull(refinedQueryString);
        assertEquals("queries should be equal", "text:\"mercury\"", refinedQueryString);

        params = new HashMap<String, String[]>();
        params.put("query", new String[]{"ioan"});
        params.put("rq", new String[]{""});
        assertEquals("should return an empty string", "", qa.createRefineSearchFilterQuery(params));

        params = new HashMap<String, String[]>();
        params.put("query", new String[]{"text:ioan"});
        params.put("rq", new String[]{"title:mercury"});
        assertEquals("queries should be equal", "title:mercury", qa.createRefineSearchFilterQuery(params));

        params = new HashMap<String, String[]>();
        params.put("query", new String[]{"ioan"});
        params.put("rq", new String[]{"text:mercury and title:romania"});
        assertEquals("queries should be equal", "text:mercury AND title:romania", qa.createRefineSearchFilterQuery(params));
    }
    */
}