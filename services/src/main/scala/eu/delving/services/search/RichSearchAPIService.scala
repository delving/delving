/*
 * Copyright 2010 DELVING BV
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

package eu.delving.services.search

import eu.europeana.core.BeanQueryModelFactory
import xml._
import org.apache.solr.client.solrj.SolrQuery
import scala.collection.JavaConversions._
import eu.europeana.core.querymodel.query._
import eu.delving.core.binding.FieldValue
import collection.mutable.LinkedHashMap
import javax.servlet.http. {HttpServletResponse, HttpServletRequest}
import net.liftweb.json. {Printer, Extraction}
import net.liftweb.json.JsonAST._
import org.apache.log4j.Logger
import java.util.{Properties, Map => JMap}
import java.net.URLEncoder
import eu.delving.sip.AccessKey
import eu.europeana.core.util.web.ClickStreamLogger
import eu.delving.services.exceptions.AccessKeyException

class RichSearchAPIService(request: HttpServletRequest, httpResponse: HttpServletResponse,
                           beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties,
                           queryAnalyzer: QueryAnalyzer, accessKey: AccessKey, clickStreamLogger: ClickStreamLogger
                                  ) {
  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")
  val restrictedApiAccess = launchProperties.getProperty("services.api.useAccessKey").toBoolean
  val cacheUrl = launchProperties.getProperty("cacheUrl") + "id="
  val portalName = launchProperties.getProperty("portal.name")
  val briefDocSearch = portalBaseUrl + "/" + portalName + "/search"
  val prettyPrinter = new PrettyPrinter(150, 5)
  val params = asScalaMap(request.getParameterMap.asInstanceOf[JMap[String, Array[String]]])

  private val log : Logger = Logger.getLogger("RichSearchAPIService")

  def parseRequest() : String = {
    val format = params.getOrElse("format", Array[String]("default")).head

    val response = try {
      if (restrictedApiAccess) {
        val wskey = params.getOrElse("wskey", Array[String]("unknown")).head
        if (!accessKey.checkKey(wskey)) {
          log.warn(String.format("Service Access Key %s invalid!", wskey));
          throw new AccessKeyException(String.format("Access Key %s not accepted", wskey));
        }
      }
      format match {
        case "json" => getJsonResultResponse()
        case "jsonp" =>
          getJsonResultResponse(params.get("callback").getOrElse(Array[String]("delvingCallback")).head)
        case _ => getXMLResultResponse(true)
      }
    }
    catch {
      case ex : Exception =>
        log.error(ex)
        errorResponse(errorMessage = ex.getLocalizedMessage)
    }
    httpResponse setCharacterEncoding ("UTF-8")
    response
  }

  private def getBriefResultsFromSolr: BriefBeanView = {
    val userQuery = request.getParameter("query")
    require(!userQuery.isEmpty)
    val jParams = request.getParameterMap.asInstanceOf[JMap[String, Array[String]]]
    val solrQuery: SolrQuery = SolrQueryUtil.createFromQueryParams(jParams, queryAnalyzer)
    solrQuery.setFields("*,score")
    val briefResultView = beanQueryModelFactory.getBriefResultView(solrQuery, solrQuery.getQuery, jParams)
    clickStreamLogger.logApiBriefView(request, briefResultView, solrQuery)
    briefResultView
  }

  private def getFullResultsFromSolr : FullBeanView = {
    val idQuery = request.getParameter("id")
    require(!idQuery.isEmpty)
    val jParams = request.getParameterMap.asInstanceOf[JMap[String, Array[String]]]
    val fullView = beanQueryModelFactory.getFullResultView(jParams)
    clickStreamLogger.logApiFullView(request, fullView, idQuery)
    fullView
  }

  private def renderRecord(doc : BriefDoc) : Elem = {
    val response =
      <item>
        {
        for (field <- doc.getFieldValuesFiltered(false, Array("delving_pmhId")).sortWith((fv1, fv2) => fv1.getKey < fv2.getKey)) yield
        renderFields(field)
        }
      </item>
    response
  }

  private def renderFields(field : FieldValue) : Seq[Elem] = {
    def encodeUrl(content: String): String = {
      if (params.getOrElse("cache", Array[String]("false")).head.contains("true") && field.getKey == "europeana_object")
        cacheUrl + URLEncoder.encode(content, "utf-8")
      else if (content.startsWith("http://")) content.replaceAll("&", "&amp;")
      else content
    }

    field.getValueAsArray.map(value =>
      try {
        XML.loadString(format("<%s>%s</%s>\n", field.getKeyAsXml, encodeUrl(value), field.getKeyAsXml))
      }
      catch {
        case ex : Exception =>
          log error ("unable to parse " + value + "for field " + field.getKeyAsXml)
          <error/>
      }
    ).toSeq
  }

  private def renderPageLink(pageLink : PageLink) : Elem = {
    <link start={pageLink.getStart.toString} isLinked={pageLink.isLinked.toString}>{pageLink.getDisplay}</link>
  }

  private def renderFacetQueryLinks(fql : FacetQueryLinks) : Elem = {
    <facet name={fql.getType} isSelected={fql.isSelected.toString}>
      {for (link <- fql.getLinks)
        yield <link url={minusAmp(link.getUrl)} isSelected={link.isRemove.toString} value={link.getValue} count={link.getCount.toString}>{link.getValue} ({link.getCount})</link>
        }
    </facet>
  }

  private def minusAmp(link : String) = link.replaceAll("amp;", "").replaceAll(" ","%20").replaceAll("qf=","qf[]=")

  def getXMLResultResponse(authorized : Boolean = true) : String = {
    httpResponse setContentType ("text/xml")
    require(params.contains("query") || params.contains("id") || params.contains("explain"))
    val response = if (params.contains("explain")) renderExplainInXml
    else if (params.containsKey("id") && !params.get("id").isEmpty) renderFullResult(authorized)
    else renderBriefResult(authorized)
    "<?xml version='1.0' encoding='utf-8' ?>\n" + prettyPrinter.format(response)
  }

  def renderFullResult(authorized : Boolean) : Elem = {
    val fullResult = getFullResultsFromSolr

    val response : Elem =
      <result xmlns:icn="http://www.icn.nl/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:dc="http://purl.org/dc/elements/1.1/"
               xmlns:raw="http://delving.eu/namespaces/raw" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ese="http://www.europeana.eu/schemas/ese/"
               xmlns:abm="http://to_be_decided/abm/">
        <item>
        {
        for (field <- fullResult.getFullDoc.getFieldValuesFiltered(false, Array("delving_pmhId")).sortWith((fv1, fv2) => fv1.getKey < fv2.getKey)) yield
        renderFields(field)
        }
        </item>
      </result>
    response
  }

  def renderBriefResult(authorized : Boolean) : Elem = {
    val briefResult = getBriefResultsFromSolr
    val pagination = briefResult.getPagination
    val searchTerms = pagination.getPresentationQuery.getUserSubmittedQuery
    val startPage = pagination.getStart

    val layoutMap = LinkedHashMap[String, String]("#thumbnail" -> "europeana:object", "#title" -> "dc:title", "#uri" -> "europeana:uri",
      "#isShownAt" -> "europeana:isShownAt", "Creator" -> "dc:creator", "Description" -> "dc:description",
      "Subject(s)" -> "dc:subject", "County" -> "abm:county", "Municipality" -> "abm:municipality", "Place" -> "abm:namedPlace",
      "Person(s)" -> "abm:aboutPerson")

    val response : Elem =
      <results xmlns:icn="http://www.icn.nl/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:dc="http://purl.org/dc/elements/1.1/"
               xmlns:raw="http://delving.eu/namespaces/raw" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ese="http://www.europeana.eu/schemas/ese/"
               xmlns:abm="http://to_be_decided/abm/">
        <query numFound={pagination.getNumFound.toString}>
            <terms>{searchTerms}</terms>
            <breadCrumbs>
              {pagination.getBreadcrumbs.map(bc => <breadcrumb field={bc.getField} href={minusAmp(bc.getHref)} value={bc.getValue}>{bc.getDisplay}</breadcrumb>)}
            </breadCrumbs>
        </query>
        <pagination>
            <start>{pagination.getStart}</start>
            <rows>{pagination.getRows}</rows>
            <numFound>{pagination.getNumFound}</numFound>
            {if (pagination.isNext) <nextPage>{pagination.getNextPage}</nextPage>}
            {if (pagination.isPrevious) <previousPage>{pagination.getPreviousPage}</previousPage>}
            <currentPage>{pagination.getStart}</currentPage>
            <links>
              {for (pageLink <- pagination.getPageLinks) yield
              renderPageLink(pageLink)}
            </links>
        </pagination>
        <layout>
          <drupal>
            {layoutMap.map(item =>
              <field>
                <key>{item._1}</key>
                <value>{item._2}</value>
              </field>
              )
            }
          </drupal>
        </layout>
        <items>
          {for (item <- briefResult.getBriefDocs) yield
            renderRecord(item)}
        </items>
        <facets>
            {for (facetQuerytLink <- briefResult.getFacetQueryLinks) yield
            renderFacetQueryLinks(facetQuerytLink)}
        </facets>
      </results>
    response
  }

  def errorResponse(error : String = "Unable to respond to the API request",
                    errorMessage: String = "Unable to determine the cause of the Failure") : String = {

    httpResponse setStatus (HttpServletResponse.SC_BAD_REQUEST)

     val response =
       <results>
         <error>
           <title>{error}</title>
           <description>{errorMessage}</description>
         </error>
       </results>

    "<?xml version='1.0' encoding='utf-8' ?>\n" + prettyPrinter.format(response)
  }

  def getJsonResultResponse(callback : String = "") : String = {
    implicit val formats = net.liftweb.json.DefaultFormats
    httpResponse setContentType ("text/javascript")

    def jsonErrorResponse(message : String = "") : String = {
      httpResponse setStatus (HttpServletResponse.SC_BAD_REQUEST)
      val docMap = Map[String, String]("status" -> "error", "message" -> message)
      Printer pretty (render (Extraction.decompose(docMap) ))
    }

    try {
      val output : List[Map[String, Any]] = if (params.containsKey("id") && !params.get("id").isEmpty) {
        val recordMap = LinkedHashMap[String, Any]()
        getFullResultsFromSolr.getFullDoc.getFieldValuesFiltered(false, Array("delving_pmhId")).foreach{
          fieldValue =>
            if (fieldValue.isNotEmpty) {
              if (fieldValue.getValueAsArray.size > 1) {
                recordMap.put(fieldValue.getKeyAsXml, encodeUrl(fieldValue.getValueAsArray, fieldValue.getKey))
              }
              else {
                recordMap.put(fieldValue.getKeyAsXml, encodeUrl(fieldValue))
              }
            }
        }
        List(recordMap.toMap)
      }
      else getBriefResultsFromSolr.getBriefDocs.map(doc => renderJsonRecord(doc)).toList

      val allItems = Map[String, List[Map[String, Any]]]("items" -> output)
      val outputJson = Printer.pretty(render(Extraction.decompose(allItems)))

      if (!callback.isEmpty) {
//        httpResponse setContentType ("application/jsonp")
        format("%s(%s)", callback, outputJson)
      }
      else
        outputJson
    }
    catch {
      case ex: Exception =>
      jsonErrorResponse(ex.getMessage)
    }
  }

  private def encodeUrl(field: FieldValue): String = {
    if (params.getOrElse("cache", Array[String]("false")).head.contains("true") && field.getKey == "europeana_object")
      cacheUrl + URLEncoder.encode(field.getFirst, "utf-8")
    else field.getFirst
  }

  private def encodeUrl(fields: Array[String], label: String): Array[String] = {
    if (params.getOrElse("cache", Array[String]("false")).head.contains("true") && label == "europeana_object")
      fields.map(fieldEntry => cacheUrl + URLEncoder.encode(fieldEntry, "utf-8"))
    else fields
  }

  def renderJsonRecord(doc : BriefDoc) : Map[String, Any] = {

    val recordMap = LinkedHashMap[String, Any]()
    val labelPairs = List[RecordLabel](
      RecordLabel("type","europeana_type"), RecordLabel("label", "dc_title"), RecordLabel("id", "dc_identifier"),
      RecordLabel("link", "europeana_isShownAt"), RecordLabel("county", "abm_county"),
      RecordLabel("geography", "dcterms_spatial", true), RecordLabel("thumbnail", "europeana_object"),
      RecordLabel("description", "dc_description", true), RecordLabel("created", "dcterms_created"),
      RecordLabel("municipality", "abm_municipality")
    )

    labelPairs.sortBy(label => label.name < label.name).foreach(label =>
      {
        val fieldValue = doc.getFieldValue(label.fieldValue)
        if (fieldValue.isNotEmpty) {
          if (label.multivalued) {recordMap.put(label.name, fieldValue.getValueAsArray)} else {recordMap.put(label.name, encodeUrl(fieldValue))}
        }
      }
    )
    recordMap.toMap
  }

  private def renderExplainBlock(label: String, options: List[String] = List(), description: String = "") : Elem = {
    <element>
            <label>{label}</label>
            {if (!options.isEmpty) <options>{options.map(option => <option>{option}</option>)}</options>}
            {if (!description.isEmpty) <description>{description}</description>}
    </element>
  }

  def renderExplainInXml : Elem = {
    val excludeList = List("europeana_unstored", "europeana_source", "europeana_userTag", "europeana_collectionTitle")
    <results>
      <api>
       <parameters>
         {renderExplainBlock("query", List("all string"))}
         {renderExplainBlock("format", List("xml", "json", "jsonp"))}
         {renderExplainBlock("cache", List("true", "false"), "Use Services Module cache for retrieving the europeana:object")}
         {renderExplainBlock("id", List("valid europeana_uri identifier"), "Will output a full-view")}
         {renderExplainBlock("start", List("non negative integer"))}
         {renderExplainBlock("qf", List("any valid Facet as defined in the facets block"))}
        </parameters>
        <search-fields>
          {beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFieldNameList.
                filterNot(field => excludeList.contains(field)).map(facet => renderExplainBlock(facet))}
        </search-fields>
        <facets>
          {beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFacetMap.map(facet => renderExplainBlock(facet._1))}
        </facets>
      </api>
    </results>
  }
}

case class RecordLabel(name : String, fieldValue : String, multivalued : Boolean = false)

object RichSearchAPIService {

  def processRequest(request: HttpServletRequest, response: HttpServletResponse, beanQueryModelFactory: BeanQueryModelFactory,
                     launchProperties: Properties, queryAnalyzer: QueryAnalyzer, accessKey: AccessKey,
                     clickStreamLogger : ClickStreamLogger) : String = {
    val service = new RichSearchAPIService(request, response, beanQueryModelFactory, launchProperties, queryAnalyzer, accessKey, clickStreamLogger)
    service parseRequest
  }

}