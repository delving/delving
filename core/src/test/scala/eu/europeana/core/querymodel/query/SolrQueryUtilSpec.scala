package eu.europeana.core.querymodel.query

import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.SolrQuery
import java.util.{Map => JMap}
import collection.mutable.HashMap
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.scalatest.junit.JUnitRunner

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class SolrQueryUtilSpec extends Spec with ShouldMatchers with DelvingTestUtil {

  private val analyser: QueryAnalyzer = getQueryAnalyser

  describe("A SolrQueryUtil") {

    describe("(when given a SolrQuery with filterqueries)") {
      val solrQuery = new SolrQuery

      it("should enclose the filterqueries with double quotes before sending to Solr") {
        solrQuery.setFilterQueries("PROVIDER:The European Library", "LANGUAGE:en")
        val phrased = SolrQueryUtil.getFilterQueriesAsPhrases(solrQuery)
        hasDoubleQuotesAroundQueryValue(phrased) should be(true)
      }

      it("should remove the double quotes from the filterqueries after receiving results from Solr") {
        val phrasedSolrQuery = solrQuery.setFilterQueries("PROVIDER:\"The European Library\"", "LANGUAGE:\"en\"")
        val unphrased = SolrQueryUtil.getFilterQueriesWithoutPhrases(phrasedSolrQuery)
        hasDoubleQuotesAroundQueryValue(unphrased) should be(false)
      }

      def hasDoubleQuotesAroundQueryValue(fq: Array[String]): Boolean =
        fq.forall {
          fq =>
            val value = fq.split(":").last
            value.startsWith("\"") && value.endsWith("\"")
        }
    }

    describe("(when given a solr query with filterqueries for the same facet)") {
      val solrQuery = new SolrQuery
      solrQuery.setFilterQueries("PROVIDER:\"The European Library\"", "LANGUAGE:\"en\"", "LANGUAGE:\"de\"")
      val facetMap: JMap[String, String] = asJavaMap(HashMap("PROVIDER" -> "prov", "LANGUAGE" -> "lang"))
      val queriesAsOrQueries = SolrQueryUtil.getFilterQueriesAsOrQueries(solrQuery, facetMap)

      it("should have an OR between the facets with the same facet field") {
        queriesAsOrQueries.toList should equal(List("{!tag=lang}LANGUAGE:(\"de\" OR \"en\")", "{!tag=prov}PROVIDER:\"The European Library\""))
      }
    }

    describe("(when give request parameters to create a SolrQuery)") {
      def compareListToQuery(paramsList: List[(String, String)], queryType: String = "simple")(queryString: String) =
        SolrQueryUtil.createFromQueryParams(createParamsMap(paramsList), analyser).toString should equal (new SolrQuery(queryString).setQueryType(queryType).toString)

      it("should ignore 'zoeken_in' when the value is 'text'") {
        compareListToQuery(List("query" -> "max", "zoeken_in" -> "text")) {"max"}
      }

      it("should surround the query with double quotes when 'zoeken_in' is not 'text") {
        compareListToQuery(List("query" -> "max devrient", "zoeken_in" -> "dc_title"), "advanced") {"dc_title:\"max devrient\""}
      }
    }

  }

}