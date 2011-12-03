package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import org.apache.solr.client.solrj.SolrQuery
import scala.collection.JavaConversions._
import java.util.{Locale, List}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class ResultPaginationSpec extends Spec with ShouldMatchers {
  val breadcrumbFactory = new BreadcrumbFactory

  describe("A ResultPagination") {

    describe("(when a start pagination range)") {
      testPagination(300, 20, 0, "simple query")
    }

    describe("(when given a middle pagination range)"){
      testPagination(300, 20, 140, "simple query")
    }

    describe("(when given a near end pagination range)"){
      testPagination(300, 20, 260, "simple query")
    }

    describe("(when given an end pagination range)"){
      testPagination(300, 20, 280, "simple query")
    }

    describe("(when given a non inclusive end pagination range)"){
      testPagination(295, 20, 280, "simple query")
    }
  }

  def testPagination(numFound: Int, rows: Int, start: Int, query: String) : Unit = {
     val pager: ResultPagination = makePage(numFound, rows, start, query)

    it("should not have a previous page if start is less then number of rows") {
      if (rows > start)
        pager.isPrevious should be (false)
      else
        pager.isPrevious should be (true)

    }

    it("should have a next page when start + rows is smaller then numFound") {
      if ((start + rows) >= numFound)
        pager.isNext should be (false)
      else
        pager.isNext should be (true)
    }

    it("should start should be start + 1") {
      pager.getStart should equal(start + 1)
    }

    it("should nextPage should be start + rows + 1") {
      pager.getNextPage should equal(start + 1 + rows)
    }

    it("should give back numFound unmodified") {
      pager.getNumFound should equal(numFound)
    }

    it("should give back rows unmodified") {
      pager.getRows should equal(rows)
    }

    it("should have pageNumber that is start divided by rows + 1") {
      pager.getPageNumber should equal(start / rows + 1)
    }

    it("should give back number of rows + start as last viewable record") {
      val lastViewableRecord = if (start + rows > numFound) numFound else start + rows
      pager.getLastViewableRecord should equal(lastViewableRecord)
    }

    it("should give back 10 pagelinks when numfound exceeds 10 times the number of rows") {
      if (numFound > (10 * rows))
        pager.getPageLinks.size should equal(10)
    }

    it("should give back numFound divided by rows with numFound is less then 10 times the number of rows") {
      if (numFound < (10 * rows))
        pager.getPageLinks.size should equal(numFound / rows)
    }

    it("should have only the first pageLink marked as not linked") {
      val notLinkedPageLink = pager.getPageLinks.filter(_.isLinked == false)
      notLinkedPageLink.size should equal(1)
      notLinkedPageLink.head.getDisplay should equal (start / rows + 1)
    }

    it("should have each pagelink, that is linked, as start the display value - 1 + number for rows") {
      val pageLinks: List[PageLink] = pager.getPageLinks
      pageLinks.tail.forall(pl => ((pl.getDisplay - 1) * rows + 1) == pl.getStart) should be(true)
    }
  }

  def makePage(numFound: Int, rows: Int, start: Int, query: String): ResultPagination = {
    def createSolrQuery(): SolrQuery = {
      val solrQuery = new SolrQuery
      solrQuery setQuery query
      solrQuery setRows rows
      solrQuery setStart start
      solrQuery
    }
    new ResultPaginationImpl(createSolrQuery, numFound, query, null, breadcrumbFactory, Locale.CANADA)
  }
}