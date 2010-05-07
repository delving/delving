package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.SolrQuery
import java.util.{Map => JMap}
import collection.mutable.HashMap

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class SolrQueryUtilSpec extends Spec with ShouldMatchers {
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
      val facetMap: JMap[String, String] = asMap(HashMap("PROVIDER" -> "prov", "LANGUAGE" -> "lang"))
      val queriesAsOrQueries = SolrQueryUtil.getFilterQueriesAsOrQueries(solrQuery, facetMap)

      it("should have an OR between the facets with the same facet field") {
        queriesAsOrQueries.toList should equal (List("{!tag=lang}LANGUAGE:(\"de\" OR \"en\")", "{!tag=prov}PROVIDER:\"The European Library\""))
      }
    }

  }

}