package eu.delving.core.binding

import java.util. {List => JList, Map => JMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

/**
 * Example Freemarker usage:
 *
 *  <#list queryParamList.getListFiltered(true, ['view', 'tab', 'orderBy']) as qp>
 *    ${qp.key} ${qp.getFirst()}
 *  </#list>
 *
 *
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 1:11 PM  
 */

object FreemarkerUtil {
  def createQueryParamList(params: JMap[String, Array[String]]) = QueryParamList(asScalaMap(params))
}

case class QueryParamList(params: Map[String, Array[String]]) {
  private val searchParams = List("query", "qf", "start", "page", "pageId")
  private val defaultParams = List("view", "tab", "sortOrder", "sortBy", "debug", "test")

  def formatAsUrl(params : JList[QueryParam]) : String = params.map(param => param.format).mkString("&")

  def getList : JList[QueryParam] = asJavaList(params.toList.map(qp => QueryParam(qp._1, qp._2)))

  def getListFormatted : String = formatAsUrl(getList)

  def getListFiltered(include: Boolean, fields: Array[String]) : JList[QueryParam] = {
    asJavaList(params.filterKeys(qp => fields.contains(qp) == include).toList.map(qp => QueryParam(qp._1, qp._2)))
  }

  def getListFilteredFormatted(include: Boolean, fields: Array[String]) : String = formatAsUrl(getListFiltered(include, fields))

  def getDefaultParams : JList[QueryParam] = getListFiltered(true, defaultParams.toArray[String])

  def getDefaultParamsFormatted : String = formatAsUrl(getDefaultParams)

  def getSearchParams : JList[QueryParam] = getListFiltered(true, searchParams.toArray[String])

  def getSearchParamsFormatted : String = formatAsUrl(getSearchParams)

  def hasKey(key : String) = params.contains(key)

  def getQueryParam(key : String) = params.get(key).getOrElse(Array(""))

  def getKeys : JList[String] = asJavaList(params.keys.toList)
}

case class QueryParam(key: String, values: Array[String]) {
  def isNotEmpty = values.length != 0
  def format = values.map(param => key + "=" + param).mkString("&")
  def getFirst = values.headOption.getOrElse("")
  def getKey = key
}
