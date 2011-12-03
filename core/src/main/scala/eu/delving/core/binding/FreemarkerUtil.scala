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

package eu.delving.core.binding

import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import eu.delving.core.storage.StaticRepo
import eu.delving.core.storage.StaticRepo.Page
import java.util.{Locale, List => JList, Map => JMap}
import java.lang.String

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

class FreemarkerUtil {

  def getStaticPage(pagePath : String, locale : String) =  StaticPage(staticPageRepo.getPage(pagePath), locale)

  def getStaticMenu(menuName : String, locale : String) =  staticPageRepo.getMenu(menuName, new Locale(locale))
  @Autowired @BeanProperty var staticPageRepo:  StaticRepo = _
}

object FreemarkerUtil {
  def createQueryParamList(params: JMap[String, Array[String]]) = QueryParamList(asScalaMap(params))
}

case class StaticPage(page : Page, language : String = "en") {
  val locale: Locale = new Locale(language)
  def getContent = {
    val content: String = page.getContent(locale)
    if (!content.startsWith("<a href")) content
    else ""
  }
  def getTitle = page.getTitle(locale)
  def getMenuName = page.getMenuName()
}

case class QueryParamList(params: Map[String, Array[String]]) {
  private val searchParams = List("query", "qf", "start", "page", "pageId")
  private val defaultParams = List("view", "tab", "sortOrder", "sortBy", "debug", "test", "theme")

  def formatAsUrl(params : JList[QueryParam]) : String = params.map(param => param.format).mkString("&amp;")

  def getList : JList[QueryParam] = asJavaList(params.toList.map(qp => QueryParam(qp._1, qp._2)))

  def getListFormatted : String = formatAsUrl(getList)

  def getListFiltered(include: Boolean, fields: Array[String]) : JList[QueryParam] = {
    asJavaList(params.filterKeys(qp => fields.contains(qp) == include).toList.map(qp => QueryParam(qp._1, qp._2)))
  }

  def getQfFiltered(include: Boolean, field: String) : QueryParam = {
    QueryParam("qf", params.get("qf").getOrElse(Array[String]()).filter(qfField => qfField.startsWith(field) == include))
  }

  def getListFilteredFormatted(include: Boolean, fields: Array[String]) : String = formatAsUrl(getListFiltered(include, fields))

  def getDefaultParams : JList[QueryParam] = getListFiltered(true, defaultParams.toArray[String])

  def getDefaultParamsFormatted : String = formatAsUrl(getDefaultParams)

  def getSearchParams : JList[QueryParam] = getListFiltered(true, searchParams.toArray[String])

  def getSearchParamsFormatted : String = formatAsUrl(getSearchParams)

  def hasKey(key : String) = params.contains(key) && getQueryParam(key).isNotEmpty

  def getQueryParam(key : String) : QueryParam = QueryParam(key, params.get(key).getOrElse(Array[String]()))

  def getKeys : JList[String] = asJavaList(params.keys.toList)
}

case class QueryParam(key: String, values: Array[String]) {
  def isNotEmpty = values.length != 0
  def format = values.map(param => key + "=" + param).mkString("&amp;")
  def getFirst = values.headOption.getOrElse("")
  def getKey = key
  def getValues = values
}
