package eu.europeana.core.querymodel.query

import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import collection.mutable.Stack

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 2, 2010 12:44:15 AM
 */

class BreadCrumbSpec extends Spec with ShouldMatchers {

  // normal spec
  describe("A BreadCrumb") {

    describe("(when given a SolrQuery without FilterQueries)") {
      import _root_.org.apache.solr.client.solrj.SolrQuery
      val queryString = "single query"
      val solrQuery = new SolrQuery(queryString)
      val list = Breadcrumb.createList(solrQuery)

      it("should only contain the query") {
        list.size should equal(1)
//        stack.size should equal(oldSize - 1)
      }

      it("should have the query flagged as last") {
//        result should equal(2)
        val breadcrumb: Breadcrumb = list.get(0)
        breadcrumb.getLast should be(true)
      }

    }

    describe("(when empty)") {
      import _root_.org.apache.solr.client.solrj.SolrQuery
      val query = new SolrQuery
      val stack = new Stack[Int]

      it("should be empty") {
        stack should be('empty)
      }

      it("should complain when popped") {
        evaluating {stack.pop()} should produce[NoSuchElementException]
      }
    }
  }
}