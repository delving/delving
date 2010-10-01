package eu.europeana.core.querymodel.query

import eu.europeana.core.querymodel.beans.{AllFieldBean, FullBean, BriefBean, IdBean}
import eu.europeana.definitions.annotations.AnnotationProcessorImpl
import collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.mock.web.MockHttpServletRequest

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 1, 2010 11:30:16 PM
 */

trait DelvingTestUtil {

  // spring-based wiring
  private val context = new ClassPathXmlApplicationContext("/core-application-context.xml")
  private val beanProcessor  = context.getBean("annotationProcessor").asInstanceOf[AnnotationProcessorImpl]
  private val analyzerBean = context.getBean("queryAnalyser").asInstanceOf[QueryAnalyzer]

  // hard wired
  private val processor: AnnotationProcessorImpl = new AnnotationProcessorImpl()
  val beanClasses = new ListBuffer[Class[_]] += (classOf[IdBean], classOf[BriefBean], classOf[FullBean], classOf[AllFieldBean])
  processor.setClasses(beanClasses.toList)

  private val analyser: QueryAnalyzer = new QueryAnalyzer
  analyser.setAnnotationProcessor(processor)

  def getAnnotationProcessor: AnnotationProcessorImpl = processor

  def getQueryAnalyser = analyser

  def getAnnotationProcessorFromBean : AnnotationProcessorImpl = beanProcessor

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