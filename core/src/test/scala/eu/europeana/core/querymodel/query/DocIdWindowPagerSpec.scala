/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.querymodel.query

import _root_.org.apache.solr.client.solrj.SolrQuery
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.springframework.mock.web.MockHttpServletRequest
import org.scalatest.{BeforeAndAfterAll, PrivateMethodTester, Spec}
import eu.delving.metadata.MetadataModelImpl
import scala.collection.JavaConversions._
import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
//@RunWith(classOf[SpringJUnit4ClassRunner])
//@ContextConfiguration(locations = Array[String](
//        "/core-application-context.xml",
//        "/test-application-context.xml"
//))
class DocIdWindowPagerSpec extends Spec with BeforeAndAfterAll with ShouldMatchers with PrivateMethodTester with SolrTester with DelvingTestUtil {
  // todo fix this unit test

  override def afterAll() = stopSolrServer
//
//  @Autowired var windowPagerFactory: DocIdWindowPagerFactory = _
//  val testCollId = "00101"
//  val solrSever = getSolrSever
//  loadDefaultData(solrSever, 14, testCollId)
//
//  /* methods to tested
//  *
//  * - fetchParameter
//  * - setQueryStringForPaging
//  * - setReturnToResults
//  * - setNextAndPrevious
//  * - getSolrStart
//  * - getFullDocInt
//  */
//
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
        pager.getQueryStringForPaging should equal("query=sjoerd&startPage=1")
      }
    }
//
//    describe("(when given the second record that has a next and previous record)") {
//
//      it("should return 14 results") {
//        val solrQuery = new SolrQuery("*:*")
//        val response = solrSever.query(solrQuery)
//        response.getResults().getNumFound should equal(14)
//      }
//
//      val (pager, uri) = createPager("2", "1", "*:*")
//
//      it("should have previous as true") {
//        pager.isPrevious should be(true)
//      }
//
//      it("should have next as true") {
//        pager.isNext should be(true)
//      }
//
//      it("should have give back the same uri it was given as current FullDoc") {
//        pager.getFullDocUri should equal(uri)
//      }
//
//      it("should have previous int as 1") {
//        pager.getPreviousInt should be(1)
//      }
//
//      it("should have next int as 3") {
//        pager.getNextInt should be(3)
//      }
//
//      it("should return only 3 ids") {
//        pager.getDocIdWindow.getIds.size should equal(3)
//      }
//    }
//
//    describe("(when given the first record that has no previous record)") {
//
//      val (pager, uri) = createPager("1", "1", "*:*")
//
//      it("should have previous as true") {
//        pager.isPrevious should be(false)
//      }
//
//      it("should have next as true") {
//        pager.isNext should be(true)
//      }
//
//      it("should have give back the same uri it was given as current FullDoc") {
//        pager.getFullDocUri should equal(uri)
//      }
//
//      it("should have previous int as 0") {
//        pager.getPreviousInt should be(0)
//      }
//
//      it("should have next int as 2") {
//        pager.getNextInt should be(2)
//      }
//
//      it("should return only 3 ids") {
//        pager.getDocIdWindow.getIds.size should equal(3)
//      }
//    }
//
//    describe("(when given the last record that has no next record)") {
//
//      val (pager, uri) = createPager("14", "13", "*:*")
//
//      it("should have previous as true") {
//        pager.isPrevious should be(true)
//      }
//
//      it("should have next as false") {
//        pager.isNext should be(false)
//      }
//
//      it("should have give back the same uri it was given as current FullDoc") {
//        pager.getFullDocUri should equal(uri)
//      }
//
//      it("should have previous int as 13") {
//        pager.getPreviousInt should be(13)
//      }
//
//      it("should have next int as 3") {
//        pager.getNextInt should be(0)
//      }
//
//      it("should return only 2 ids") {
//        pager.getDocIdWindow.getIds.size should equal(2)
//      }
//    }
//
//    describe("(when given an out of bounds record)") {
//
//      val (pager, uri) = createPager("25", "23", "*:*")
//
//      it("should return null") {
//        pager should equal(null)
//      }
//    }
//
//    describe("(when given an negative bounds record)") {
//
//      val (pager, uri) = createPager("-1", "-3", "*:*")
//
//      it("should return null") {
//        pager should equal(null)
//      }
//    }
//
//    describe("(when given a a record coming from the brief-result page)") {
//
//      val (pager, uri) = createPager("2", "1", "*:*")
//
//      it("should give back a return to results uri") {
//        pager.getReturnToResults should equal("/portal/search?query=*%3A*&amp;start=1&amp;view=table&amp;tab=all&amp;rtr=true")
//      }
//    }
//  }
//
//  def createPager(start: String, startPage: String, userQuery: String): (DocIdWindowPager, String) = {
//    val request: MockHttpServletRequest = new MockHttpServletRequest
//    List[(String, String)]("startPage" -> startPage, "start" -> start, "query" -> userQuery, "pageId" -> "brd").foreach(param =>
//      request.addParameter(param._1, param._2))
//    val uri = createEuropeanaUri(start, testCollId)
//    val query = SolrQueryUtil.createFromQueryParams(request.getParameterMap, getQueryAnalyser, getThemeHandler.getDefaultTheme.getRecordDefinition)
//    request.addParameter("uri", uri)
//    val pager: DocIdWindowPager = windowPagerFactory.getPager(request.getParameterMap, query, getRecordDefinition)
//    (pager, uri)
  }

}