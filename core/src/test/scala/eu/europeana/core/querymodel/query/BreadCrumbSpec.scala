package eu.europeana.core.querymodel.query

import _root_.java.net.URLEncoder
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.apache.solr.client.solrj.SolrQuery
import scala.collection.JavaConversions._
import collection.immutable.List
import java.util.Locale
import eu.delving.core.util.LocalizedFieldNames

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 2, 2010 12:44:15 AM
 */

@RunWith(classOf[JUnitRunner])
class BreadCrumbSpec extends Spec with ShouldMatchers {
  val bcFactory = new BreadcrumbFactory
  val queryString = "single query"
  val encodedQueryString = URLEncoder.encode(queryString, "utf-8")
  val filterPrefix = "&amp;qf="
  val queryPrefix = "query=" + encodedQueryString;

  describe("A List of BreadCrumbs") {

    describe("(when given a SolrQuery without FilterQueries)") {
      val solrQuery = new SolrQuery(queryString)
      val list = (bcFactory.createList(solrQuery, Locale.CANADA)).toList

      it("should only contain the query") {
        list.size should equal(1)
      }

      val breadcrumb: Breadcrumb = list.head

      it("should have the query as the first item") {
        breadcrumb.getDisplay should equal(queryString)
        breadcrumb.getHref should equal(queryPrefix)
      }

      it("should have the query flagged as last") {
        breadcrumb.isLast should be(true)
      }

    }

    describe("(when given a SolrQuery with FilterQueries)") {
      val solrQuery = new SolrQuery(queryString)
      val filterQueries: List[String] = List("YEAR:1900", "YEAR:1901", "LOCATION:Here")
      solrQuery setFilterQueries (filterQueries: _*)
      val list = bcFactory.createList(solrQuery, Locale.CANADA).toList

      it("should contain as many entries as filterqueries + the query") {
        list.size should equal(solrQuery.getFilterQueries.length + 1)
      }

      it("should retain the same order as the FilterQueries") {
        val displayList = for (fq <- list.tail) yield fq.getDisplay
        filterQueries should equal(displayList)
      }

      it("only the last entry should be flagged as true") {
        list.head.isLast should be(false)
        list.last.isLast should be(true)
        list.filter(_.isLast).size should be(1)
        list.filter(_.isLast == false).size should be(filterQueries.size)
      }

      it("should format the href in same order as the FilterQueries") {
        val filterBreadCrumbList = list.tail
        for (index <- 0 until filterBreadCrumbList.length) {
          filterBreadCrumbList(index).getHref should equal {
            filterQueries.take(index + 1).mkString(start = queryPrefix + filterPrefix, sep = filterPrefix, end = "")
          }
        }
      }


      it("should have all filterQueries marked as true for filterQueries and field + value should be non-empty") {
        list.head.getField.isEmpty should be (true)
        list.tail.forall(_.getField.isEmpty) should be (false)
      }
    }

    describe("(when given an empty SolrQuery)") {
      val solrQuery = new SolrQuery

      it("produces an EuropeanaQueryException") {
        evaluating {bcFactory.createList(solrQuery, Locale.CANADA)} should produce[EuropeanaQueryException]
      }
    }

    describe("(when given an illegal FilterQuery)") {
      val solrQuery: SolrQuery = new SolrQuery(queryString).setFilterQueries("LANGUAGE:en", "wrong filter query")

      it("should ignore the illegal FilterQuery") {
        val list: List[Breadcrumb] = (bcFactory.createList(solrQuery, Locale.CANADA)).toList
        list.size should equal(2)
        val lastBreadCrumb: Breadcrumb = list.last
        lastBreadCrumb.isLast should be(true)
        lastBreadCrumb.getHref should equal(queryPrefix + "&amp;qf=LANGUAGE:en")
      }
    }
  }
}