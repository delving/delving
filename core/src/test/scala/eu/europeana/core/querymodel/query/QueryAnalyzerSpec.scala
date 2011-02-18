package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import collection.immutable.List
import collection.mutable.{HashMap, ListBuffer}
import java.util.{Map => JMap, HashMap => JHashMap}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class QueryAnalyzerSpec extends Spec with ShouldMatchers {
  val qa = createQueryAnalyzer

  describe("A QueryAnalyzer") {

    describe("(when given an illegal query)") {

      it("should throw an EuropeanaQueryException") {
        evaluating {queryType("gumby:goes_bad")} should produce[EuropeanaQueryException]
      }
    }

    describe("(when given a simple query string)") {
      val simple = QueryType.SIMPLE_QUERY

      it("should be identified as a simple query") {
        queryType("a very simple query") should equal(simple)
      }
    }

    describe("(when given an advanced query)") {
      val advanced = QueryType.ADVANCED_QUERY

      it("should identify a phrase query as advanced") {
        queryType("+\"phrase query\"") should equal(advanced)
      }

      it("should identify a fielded query as advanced") {
        queryType("text:fieldedQuery") should equal(advanced)
      }

      it("should identify a boolean query as advanced") {
        queryType("advanced and Query") should equal(advanced)
      }
    }

    describe("(when given a moreLikeThis Query)") {
      val mlt = QueryType.MORE_LIKE_THIS_QUERY

      it("should identify an europeanaUri as moreLikeThis") {
        queryType("europeana_uri:\"http://www.europeana.eu/bla/bla\"") should equal(mlt)
      }
    }

    describe("(when sanitizing a query)") {
      def sanitize(query: String) = qa.sanitize(query)


      it("should remove left and right braches, e.g. { } ") {
        sanitize("{query}") should equal("query")
      }

      it("should uppercase 'or'") {
        sanitize("query or otherQuery") should equal("query OR otherQuery")
      }

      it("should uppercase 'and'") {
        sanitize("query and otherQuery") should equal("query AND otherQuery")
      }

      it("should uppercase 'not'") {
        sanitize("query not otherQuery") should equal("query NOT otherQuery")
      }

      it("should uppercase multiple boolean operators") {
        sanitize("query or otherQuery and query2 not query3") should equal("query OR otherQuery AND query2 NOT query3")
      }

    }

    describe("(when creating an AdvancedQuery from http request parameters)") {
      def advancedQuery(params: List[(String, String)]) = qa.createAdvancedQuery(params)


      it("should only use non-empty facets and queries") {
        advancedQuery(List("query1" -> "mozart")) should equal("text:mozart")
      }

      it("should use text: as default search field value") {
        advancedQuery(List("query1" -> "treasure", "query3" -> "hunter")) should equal("text:treasure text:hunter")
      }

      it("should use the facet parameters when non-empty") {
        advancedQuery(List("facet1" -> "text", "query1" -> "treasure",
          "operator2" -> "or", "facet2" -> "title", "query2" -> "chest",
          "operator3" -> "and", "facet3" -> "creator", "query3" -> "max")) should equal("text:treasure OR title:chest AND creator:max")
      }
    }

    describe("(when creating a RefineQuery from http request parameters)") {
      def refineSearch(params: List[(String, String)]) = qa.createRefineSearchFilterQuery(params)

      it("should return an empty string when the request parameter is empty") {
        refineSearch(List()).isEmpty should be(true)
      }

      it("should the refine query does not contain an search field, the 'text' search field should be used") {
        refineSearch(List("rq" -> "mercury")) should equal(""" text:"mercury" """.trim)
      }

      it("should uppercase boolean operators in the query") {
          refineSearch(List("rq" -> "text:mercury and title:romania")) should equal ("text:mercury AND title:romania")
      }

      it("should accept fielded queries") {
          refineSearch(List("rq" -> "title:romania")) should equal ("title:romania")
      }

      it("should not add default 'text' search field to boolean queries with mixed fielded and unfielded searches") {
          refineSearch(List("rq" -> "romania and title:chest")) should equal ("romania AND title:chest")
      }

    }
  }

  def createQueryAnalyzer: QueryAnalyzer = {
    val queryAnalyzer = new QueryAnalyzer
    queryAnalyzer
  }

  implicit def createParameterMap(params: List[(String, String)]): JMap[String, Array[String]] = {
    val operators = List("facet1", "query1", "operator2", "facet2", "query2", "operator3", "facet3", "query3", "rq")
    val requestParameterMap: JMap[String, Array[String]] = new JHashMap[String, Array[String]]
    operators.foreach(f => requestParameterMap put (f, Array {""}))
    params foreach {
      param =>
        param match {
          case (key, value) => requestParameterMap update (key, Array {value})
          case _ =>
        }
    }
    requestParameterMap
  }

  def queryType(query: String): QueryType = qa.findSolrQueryType(query)
}