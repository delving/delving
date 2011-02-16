package eu.europeana.core.querymodel.query

import org.springframework.mock.web.MockHttpServletRequest
import eu.delving.metadata._
/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 1, 2010 11:30:16 PM
 */

trait DelvingTestUtil {

  private val metadataModel = new MetadataModelImpl("/abm-record-definition.xml", "abm")
  private val analyzerBean = new QueryAnalyzer(metadataModel)

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