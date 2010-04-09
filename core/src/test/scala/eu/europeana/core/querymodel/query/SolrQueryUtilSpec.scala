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
class SolrQueryUtilSpec extends Spec with ShouldMatchers {


  /*
  @Test
    public void testPhraseConversions() {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFilterQueries(
                "PROVIDER:The European Library",
                "LANGUAGE:en"
        );
        String[] phrased = SolrQueryUtil.getFilterQueriesAsPhrases(solrQuery);
        for (String s : phrased) {
            String after = s.substring(s.indexOf(':') + 1);
            Assert.assertTrue(after.startsWith("\"") && after.endsWith("\""));
        }
        solrQuery.setFilterQueries(phrased);
        String [] unphrased = SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery);
        for (String s : unphrased) {
            String after = s.substring(s.indexOf(':') + 1);
            Assert.assertFalse(after.startsWith("\"") && after.endsWith("\""));
        }
    }

    @Test
    public void testOrBasedFacetQueries() throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFilterQueries(
                "PROVIDER:\"The European Library\"",
                "LANGUAGE:\"en\"",
                "LANGUAGE:\"de\""
        );
        Map<String, String> facetMap = new HashMap<String, String>();
        facetMap.put("PROVIDER", "prov");
        facetMap.put("LANGUAGE", "lang");
        String[] expectFilterQueries = new String[]{"{!tag=lang}LANGUAGE:(\"de\" OR \"en\")", "{!tag=prov}PROVIDER:\"The European Library\""};
        log.info("expecting:");
        for (String expect : expectFilterQueries) {
            log.info(expect);
        }
        String[] actualFilterQueries = SolrQueryUtil.getFilterQueriesAsOrQueries(solrQuery, facetMap);
        log.info("actual:");
        for (String actual : actualFilterQueries) {
            log.info(actual);
        }
        Assert.assertArrayEquals("arrays should be equal", expectFilterQueries, actualFilterQueries);
    }
   */
}