package eu.europeana.core.querymodel.query

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 1, 2010 11:30:16 PM
 */

trait DelvingTestUtil {

  // spring-based wiring
  private val context = new ClassPathXmlApplicationContext("/core-application-context.xml")
  private val analyzerBean = context.getBean("queryAnalyser").asInstanceOf[QueryAnalyzer]

  def getQueryAnalyserFromBean = analyzerBean

  // RequestBased helpers
  def createParamsMap(params: List[(String, String)]) = {
    val request: MockHttpServletRequest = new MockHttpServletRequest
    params.foreach(param =>
      request.addParameter(param._1, param._2))
    request.getParameterMap
  }

  def createRequest(params: List[(String, String)]) = {
    val request: MockHttpServletRequest = new MockHttpServletRequest
    params.foreach(param =>
      request.addParameter(param._1, param._2))
    request
  }
}