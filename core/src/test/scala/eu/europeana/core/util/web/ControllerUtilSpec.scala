package eu.europeana.core.querymodel.query

import _root_.eu.europeana.core.util.web.ControllerUtil
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.Spec
import _root_.org.springframework.mock.web.MockHttpServletRequest


/**
 * todo finish this spec file
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */


@RunWith(classOf[JUnitRunner])
class ControllerUtilSpec extends Spec with ShouldMatchers {

  describe("The ControllerUtil ") {

      describe("(when given a request with query parameters)") {
        val request = new MockHttpServletRequest
        val parameters = List("query" -> "sjoerd", "id" -> "007", "className" -> "SavedSearch")
        parameters foreach ( param => request setParameter (param._1, param._2) )

        it("should the query parameters format it as query string in the same order") {
          formatMap(request) should equal ( parameters map (p => p._1 + "=" + p._2) mkString ("?","&","") )
        }

        it("should give back the full url including the hostname and all parameters") {
          val hostName = "http://localhost:80"
          (ControllerUtil formatFullRequestUrl request).startsWith(hostName) should be (true)
        }

        it("should retain empty parameters") {
          request setParameter ("empty", "")
          formatMap(request) endsWith "&empty=" should be (true)
        }

        def formatMap(request: MockHttpServletRequest) = ControllerUtil.formatParameterMapAsQueryString(request.getParameterMap)
      }
  }
}