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

package eu.delving.services.search

import eu.europeana.core.BeanQueryModelFactory
import xml._
import org.apache.solr.client.solrj.SolrQuery
import scala.collection.JavaConversions._
import eu.europeana.core.querymodel.query._
import eu.delving.core.binding.FieldValue
import javax.servlet.http. {HttpServletResponse, HttpServletRequest}
import net.liftweb.json. {Printer, Extraction}
import net.liftweb.json.JsonAST._
import org.apache.log4j.Logger
import java.net.URLEncoder
import eu.delving.sip.AccessKey
import eu.europeana.core.util.web.ClickStreamLogger
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import eu.delving.services.exceptions.AccessKeyException
import java.lang.String
import eu.delving.core.util.{ThemeFilter, LocalizedFieldNames}
import java.util.{Locale, Properties, Map => JMap}
import collection.immutable.ListMap
import collection.mutable.{Map, LinkedHashMap}

class RichSearchAPIService(aro : ApiRequestObject) {

  private val log : Logger = Logger.getLogger("RichSearchAPIService")

  val format = aro.params.getOrElse("format", Array[String]("default")).head

  /**
   * This function parses the response for with output format needs to be rendered
   */

  def parseRequest : String = {

    val response = try {
      if (aro.restrictedApiAccess) {
        val wskey = aro.params.getOrElse("wskey", Array[String]("unknown")).head
        if (!aro.accessKey.checkKey(wskey)) {
          log.warn(String.format("Service Access Key %s invalid!", wskey));
          throw new AccessKeyException(String.format("Access Key %s not accepted", wskey));
        }
      }
      format match {
        case "json" => getJSONResultResponse()
        case "jsonp" =>
          getJSONResultResponse(callback = aro.params.get("callback").getOrElse(Array[String]("delvingCallback")).head)
        case "simile" => getSimileResultResponse()
        case "similep" =>
          getSimileResultResponse(aro.params.get("callback").getOrElse(Array[String]("delvingCallback")).head)
        case _ => getXMLResultResponse(true)
      }
    }
    catch {
      case ex : Exception =>
        log.error("something went wrong", ex)
        errorResponse(errorMessage = ex.getLocalizedMessage, format = format)
    }
    aro.response setCharacterEncoding ("UTF-8")
    response
  }

  def getJSONResultResponse(authorized: Boolean = true, callback : String = ""): String = {
    aro.response setContentType ("text/xml")
    require(aro.params.contains("query") || aro.params.contains("id") || aro.params.contains("explain"))

    val response : String = aro.params match {
      case x : Map[String, Array[String]] if x.containsKey("explain") => ExplainResponse(aro).renderAsJson
      case x : Map[String, Array[String]] if x.containsKey("id") && !x.get("id").isEmpty => FullView(getFullResultsFromSolr, aro).renderAsJSON(authorized)
      case _ => SearchSummary(getBriefResultsFromSolr, aro).renderAsJSON(authorized)
    }
    if (!callback.isEmpty) {
      // httpResponse setContentType ("application/jsonp")
      "%s(%s)".format(callback, response)
    }
    else
      response
  }

  def getXMLResultResponse(authorized: Boolean = true): String = {
    aro.response setContentType ("text/xml")
    require(aro.params.contains("query") || aro.params.contains("id") || aro.params.contains("explain"))

    val response : Elem = aro.params match {
      case x : Map[String, Array[String]] if x.containsKey("explain") => ExplainResponse(aro).renderAsXml
      case x : Map[String, Array[String]] if x.containsKey("id") && !x.get("id").isEmpty => FullView(getFullResultsFromSolr, aro).renderAsXML(true)
      case _ => SearchSummary(getBriefResultsFromSolr, aro).renderAsXML(authorized)
    }

    "<?xml version='1.0' encoding='utf-8' ?>\n" + aro.prettyPrinter.format(response)
  }

  private def getBriefResultsFromSolr: BriefBeanView = {
    val userQuery = aro.request.getParameter("query")
    require(!userQuery.isEmpty)
    val jParams = aro.request.getParameterMap.asInstanceOf[JMap[String, Array[String]]]
    val solrQuery: SolrQuery = SolrQueryUtil.createFromQueryParams(jParams, aro.queryAnalyzer, aro.locale, ThemeFilter.getTheme.getRecordDefinition)

    if (jParams.containsKey("fl")) solrQuery.setFields(jParams.get("fl").headOption.getOrElse("*,score")) else solrQuery.setFields("*,score")
    if (jParams.contains("facet.limit")) solrQuery.setFacetLimit(Integer.valueOf(jParams.get("facet.limit").headOption.getOrElse("100")))
    if (jParams.getOrElse("facet.missing", "false").toString.equalsIgnoreCase("true")) solrQuery.setFacetMissing(true)

    val briefResultView = aro.beanQueryModelFactory.getBriefResultView(solrQuery, solrQuery.getQuery, jParams, aro.locale)
    aro.clickStreamLogger.logApiBriefView(aro.request, briefResultView, solrQuery)
    briefResultView
  }

  private def getFullResultsFromSolr : FullBeanView = {
    val idQuery = aro.request.getParameter("id")
    require(!idQuery.isEmpty)
    val jParams = aro.request.getParameterMap.asInstanceOf[JMap[String, Array[String]]]
    val fullView = aro.beanQueryModelFactory.getFullResultView(jParams, aro.locale)
    aro.clickStreamLogger.logApiFullView(aro.request, fullView, idQuery)
    fullView
  }

  def errorResponse(error : String = "Unable to respond to the API request",
                    errorMessage: String = "Unable to determine the cause of the Failure", format : String = "xml") : String = {

    def toXML : String = {
      aro.response setContentType ("text/xml")
      val response =
        <results>
         <error>
           <title>{error}</title>
           <description>{errorMessage}</description>
         </error>
       </results>
    "<?xml version='1.0' encoding='utf-8' ?>\n" + aro.prettyPrinter.format(response)
    }

    def toJSON : String = {
      aro.response setContentType ("text/javascript")
      implicit val formats = net.liftweb.json.DefaultFormats
      val docMap = ListMap("status" -> error, "message" -> errorMessage)
      Printer pretty (render(Extraction.decompose(docMap)))
    }

    val response : String = format match {
       case x : String if x.startsWith("json") || x.startsWith("simile") => toJSON
       case _ => toXML
     }

    aro.response setStatus (HttpServletResponse.SC_BAD_REQUEST)
    response
  }

  @Deprecated
  def getSimileResultResponse(callback : String = "") : String = {
    implicit val formats = net.liftweb.json.DefaultFormats
    aro.response setContentType ("text/javascript")

    try {
      val output : ListMap[String, Any] = if (aro.params.containsKey("id") && !aro.params.get("id").isEmpty) {
        val recordMap = collection.mutable.ListMap[String, Any]()
        getFullResultsFromSolr.getFullDoc.getFieldValuesFiltered(false, Array("delving_pmhId")).foreach{
          fieldValue =>
            if (fieldValue.isNotEmpty) {
              if (fieldValue.getValueAsArray.size > 1) {
                recordMap.put(fieldValue.getKeyAsXml, RichSearchAPIService.encodeUrl(fieldValue.getValueAsArray, fieldValue.getKey, aro))
              }
              else {
                recordMap.put(fieldValue.getKeyAsXml, RichSearchAPIService.encodeUrl(fieldValue, aro))
              }
            }
        }
        ListMap("item" -> ListMap(recordMap.toSeq : _*))
      }
      else ListMap("items" -> getBriefResultsFromSolr.getBriefDocs.map(doc => renderSimileRecord(doc)))

      val outputJson = Printer.pretty(render(Extraction.decompose(output)))

      if (!callback.isEmpty) {
        // httpResponse setContentType ("application/jsonp")
        "%s(%s)".format(callback, outputJson)
      }
      else
        outputJson
      }
    catch {
      case ex: Exception =>
      log.error("something went wrong", ex)
      errorResponse(errorMessage = ex.getMessage, format = "json")
    }
  }

  @Deprecated
  def renderSimileRecord(doc: BriefDoc): ListMap[String, Any] = {

    val recordMap = collection.mutable.ListMap[String, Any]()
    val labelPairs = List[RecordLabel](
      RecordLabel("type", "europeana_type"), RecordLabel("label", "dc_title"), RecordLabel("id", "dc_identifier"),
      RecordLabel("link", "europeana_isShownAt"), RecordLabel("county", "abm_county"),
      RecordLabel("geography", "dcterms_spatial", true), RecordLabel("thumbnail", "europeana_object"),
      RecordLabel("description", "dc_description", true), RecordLabel("created", "dcterms_created"),
      RecordLabel("municipality", "abm_municipality"), RecordLabel("pid", "europeana_uri")
    )

    labelPairs.sortBy(label => label.name < label.name).foreach(label => {
      val fieldValue = doc.getFieldValue(label.fieldValue)
      if (fieldValue.isNotEmpty) {
        if (label.multivalued) {
          recordMap.put(label.name, fieldValue.getValueAsArray)
        }
        else {
          recordMap.put(label.name, RichSearchAPIService.encodeUrl(fieldValue, aro))
        }
      }
    }
    )
   ListMap(recordMap.toSeq : _*)
  }

}

case class RecordLabel(name : String, fieldValue : String, multivalued : Boolean = false)

case class SearchSummary(result : BriefBeanView, aro : ApiRequestObject) {

  private val log : Logger = Logger.getLogger("SearchSummary")
  private val pagination = result.getPagination
  private val searchTerms = pagination.getPresentationQuery.getUserSubmittedQuery
  private val startPage = pagination.getStart
  private val language = aro.params.getOrElse("lang", Array[String]("en")).head

  def minusAmp(link : String) = link.replaceAll("amp;", "").replaceAll(" ","%20").replaceAll("qf=","qf[]=")

  def localiseKey(metadataField: String, defaultLabel: String = "unknown", language: String = "en"): String = {
    val locale = new Locale(language)
    val localizedName: String = aro.lookup.toLocalizedName(metadataField.replace(":", "_"), locale)
    if (localizedName != null && !defaultLabel.startsWith("#")) localizedName else defaultLabel
  }

  val layoutMap = LinkedHashMap[String, String]("#thumbnail" -> "europeana:object", "#title" -> "dc:title", "#uri" -> "europeana:uri",
    "#isShownAt" -> "europeana:isShownAt", "#description" -> "dc:description", "Creator" -> "dc:creator",
    "Subject(s)" -> "dc:subject", "County" -> "abm:county", "Municipality" -> "abm:municipality", "Place" -> "abm:namedPlace",
    "Person(s)" -> "abm:aboutPerson")

  // workaround for to be able to use internationalisation keys for the facets
  val facetMap = LinkedHashMap[String, String]("LANGUAGE" -> "dc:language", "YEAR" -> "europeana:year", "HASDIGITALOBJECT" -> "europeana:hasDigitalObject",
    "TYPE" -> "dc:type", "PROVIDER" -> "europeana:provider", "DATAPROVIDER" -> "europeana:dataProvider", "NAMEDPLACE" -> "abm:namedPlace",
    "COUNTY" -> "abm:county", "ABOUTPERSON" -> "abm:aboutPerson", "MUNICIPALITY" -> "abm:municipality")

  def getMetadataKey(facetKey: String) = facetMap.get(facetKey).getOrElse("unknown")

  def renderAsXML(authorized : Boolean) : Elem = {

    val response : Elem =
      <results xmlns:icn="http://www.icn.nl/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:dc="http://purl.org/dc/elements/1.1/"
               xmlns:raw="http://delving.eu/namespaces/raw" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ese="http://www.europeana.eu/schemas/ese/"
               xmlns:abm="http://to_be_decided/abm/" xmlns:abc="http://www.ab-c.nl/">
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
              {pagination.getPageLinks.map(pageLink =>
              <link start={pageLink.getStart.toString} isLinked={pageLink.isLinked.toString}>{pageLink.getDisplay}</link>)}
            </links>
        </pagination>
        <layout>
          <drupal>
            {layoutMap.map(item =>
              <field>
                <key>{localiseKey(item._2, item._1, language)}</key>
                <value>{item._2}</value>
              </field>
              )
            }
          </drupal>
        </layout>
        <items>
          {result.getBriefDocs.map(item =>
          <item>
          {item.getFieldValuesFiltered(false, Array("delving_pmhId","europeana:collectionName", "europeana:collectionTitle")).sortWith((fv1, fv2) => fv1.getKey < fv2.getKey).map(field => RichSearchAPIService.renderXMLFields(field, aro))}
          </item>
        )}
        </items>
        <facets>
          {result.getFacetQueryLinks.map(fql =>
            <facet name={fql.getType} isSelected={fql.isSelected.toString} i18n={localiseKey(metadataField = getMetadataKey(fql.getType), language = language)}>
              {fql.getLinks.map(link =>
                    <link url={minusAmp(link.getUrl)} isSelected={link.isRemove.toString} value={link.getValue} count={link.getCount.toString}>{link.getValue} ({link.getCount})</link>
            )}
            </facet>
          )}
        </facets>
      </results>
    response
  }

  def renderAsJSON(authorized : Boolean) : String = {
    implicit val formats = net.liftweb.json.DefaultFormats

    def createJsonRecord(doc : BriefDoc) : ListMap[String, Any]= {
      val recordMap = collection.mutable.ListMap[String, Any]();
      doc.getFieldValuesFiltered(false, Array("delving_pmhId", "europeana:collectionName", "europeana:collectionTitle"))
                              .sortWith((fv1, fv2) => fv1.getKey < fv2.getKey).foreach(fv => recordMap.put(fv.getKeyAsXml, RichSearchAPIService.encodeUrl(fv.getValueAsArray, fv.getKey, aro)))
      ListMap(recordMap.toSeq: _*)
    }

    def createLayoutItems : ListMap[String, Any] = {
      val recordMap = collection.mutable.ListMap[String, Any]();
      layoutMap.map(item =>
              recordMap.put(localiseKey(item._2, item._1, language), item._2))
      ListMap(recordMap.toSeq: _*)
    }

    def createFacetList: List[ListMap[String, Any]] = {
      result.getFacetQueryLinks.map(fql =>
        ListMap("name" -> fql.getType, "isSelected" -> fql.isSelected, "links" -> fql.getLinks.map(link =>
          ListMap("url" -> minusAmp(link.getUrl), "isSelected" -> link.isRemove, "value" -> link.getValue, "count" -> link.getCount, "displayString" -> "%s (%s)".format(link.getValue, link.getCount))))
      ).toList
    }

    val outputJson = Printer.pretty(render(Extraction.decompose(
      ListMap("result" ->
              ListMap("query" ->
                      ListMap("numfound" -> pagination.getNumFound, "terms" -> searchTerms,
                        "breadCrumbs" -> pagination.getBreadcrumbs.map(bc => ListMap("field" -> bc.getField, "href" -> minusAmp(bc.getHref), "value" -> bc.getDisplay))),
                "pagination" ->
                        ListMap("start" -> pagination.getStart, "rows" -> pagination.getRows, "numFound" -> pagination.getNumFound,
                          "hasNext" -> pagination.isNext, "nextPage" -> pagination.getNextPage, "hasPrevious" -> pagination.isPrevious,
                          "previousPage" -> pagination.getPreviousPage, "currentPage" -> pagination.getStart,
                          "links" -> pagination.getPageLinks.map(pageLink => ListMap("start" -> pageLink.getStart, "isLinked" -> pageLink.isLinked, "pageNumber" -> pageLink.getDisplay))
                        ),
                "layout" ->
                        ListMap[String, Any]("drupal" -> createLayoutItems),
                "items" ->
                        result.getBriefDocs.map(doc => createJsonRecord(doc)).toList,
                "facets" -> createFacetList
              )
      )
    )))
    outputJson
  }
}

case class FullView(fullResult : FullBeanView, aro : ApiRequestObject) {

    def renderAsXML(authorized : Boolean) : Elem = {
      val response: Elem =
      <result xmlns:icn="http://www.icn.nl/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:dc="http://purl.org/dc/elements/1.1/"
              xmlns:raw="http://delving.eu/namespaces/raw" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ese="http://www.europeana.eu/schemas/ese/"
              xmlns:abm="http://to_be_decided/abm/" xmlns:abc="http://www.ab-c.nl/">
        <item>
          {for (field <- fullResult.getFullDoc.getFieldValuesFiltered(false, Array("delving_pmhId")).sortWith((fv1, fv2) => fv1.getKey < fv2.getKey)) yield
          RichSearchAPIService.renderXMLFields(field, aro)}
        </item>
      </result>
    response
    }

    def renderAsJSON(authorized : Boolean) : String = {
      implicit val formats = net.liftweb.json.DefaultFormats

      val recordMap = collection.mutable.ListMap[String, Any]()
      fullResult.getFullDoc.getFieldValuesFiltered(false, Array("delving_pmhId","europeana:collectionName", "europeana:collectionTitle"))
                              .sortWith((fv1, fv2) => fv1.getKey < fv2.getKey).foreach(fv => recordMap.put(fv.getKeyAsXml, fv.getValueAsArray))

      val outputJson = Printer.pretty(render(Extraction.decompose(
        ListMap("result" ->
              ListMap("item" -> ListMap(recordMap.toSeq : _*))
        )
      )))
      outputJson
    }
}

case class ExplainItem(label: String, options: List[String] = List(), description: String = "") {

  def toXML : Elem = {
    <element>
            <label>{label}</label>
            {if (!options.isEmpty) <options>{options.map(option => <option>{option}</option>)}</options>}
            {if (!description.isEmpty) <description>{description}</description>}
    </element>
  }

  def toJson : ListMap[String, Any] = {
    if (!options.isEmpty && !description.isEmpty)
      ListMap("label" -> label, "options" -> options.toSeq, "description" -> description)
    else
      ListMap("label" -> label)
  }

}

case class ExplainResponse(aro : ApiRequestObject) {
  val excludeList = List("europeana_unstored", "europeana_source", "europeana_userTag", "europeana_collectionTitle")

  val paramOptions : List[ExplainItem] = List(
         ExplainItem("query", List("any string"), "Will output a summary result set. Any valid Lucene or Solr Query syntax will work."),
         ExplainItem("format", List("xml", "json", "jsonp", "simile", "similep")),
         ExplainItem("cache", List("true", "false"), "Use Services Module cache for retrieving the europeana:object"),
         ExplainItem("id", List("any valid europeana_uri identifier"), "Will output a full-view"),
         ExplainItem("fl", List("any valid search field in a comma-separated list"), "Will only output the specified search fields"),
         ExplainItem("facet.limit", List("Any valid integer. Default is 100"), "Will limit the number of facet entries returned to integer specified."),
         ExplainItem("start", List("any non negative integer")),
         ExplainItem("qf", List("any valid Facet as defined in the facets block")),
         ExplainItem("hqf", List("any valid Facet as defined in the facets block"), "This link is not used for the display part of the API." +
              "It is used to send hidden constraints to the API to create custom API views"),
         ExplainItem("explain", List("all")),
         ExplainItem("sortBy", List("any valid sort field prefixed by 'sort_'"), "When during"),
         ExplainItem("sortOrder", List("asc", "desc"), "The sort order of the field specified by sortBy"),
         ExplainItem("lang", List("any valid iso 2 letter lang codes"), "Feature still experimental. In the future it will allow you to get " +
               "localised strings back for the metadata fields, search fields and facets blocks"),
         ExplainItem("wskey", List("any valid webservices key"), "When the API has been marked as closed")
  )
  
  def renderAsXml : Elem = {

    <results>
      <api>
       <parameters>
         {paramOptions.map(param => param.toXML)}
        </parameters>
        <search-fields>
          {aro.beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFieldNameList.
                filterNot(field => excludeList.contains(field)).map(facet => ExplainItem(facet).toXML)}
        </search-fields>
        <facets>
          {aro.beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFacetMap.map(facet => ExplainItem(facet._1).toXML)}
        </facets>
      </api>
    </results>
  }

  def renderAsJson : String = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val outputJson = Printer.pretty(render(Extraction.decompose(
      ListMap("results" ->
              ListMap("api" ->
                     ListMap(
                    // "parameters" -> paramOptions.map(param => param.toJson), todo fix this
                        "search-fields" -> aro.beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFieldNameList.
                filterNot(field => excludeList.contains(field)).map(facet => ExplainItem(facet).toJson),
                        "facets" -> aro.beanQueryModelFactory.getMetadataModel.getRecordDefinition.getFacetMap.map(facet => ExplainItem(facet._1).toJson)))
      ))
    ))
    outputJson
  }
}

case class ApiRequestObject(request: HttpServletRequest, locale: Locale, response: HttpServletResponse, beanQueryModelFactory: BeanQueryModelFactory,
                     launchProperties: Properties, queryAnalyzer: QueryAnalyzer, accessKey: AccessKey,
                     clickStreamLogger : ClickStreamLogger, lookup : LocalizedFieldNames.Lookup) {

  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")
  val restrictedApiAccess = launchProperties.getProperty("services.api.useAccessKey").toBoolean
  val cacheUrl = launchProperties.getProperty("cacheUrl") + "id="
  val portalName = launchProperties.getProperty("portal.name")
  val briefDocSearch = portalBaseUrl + "/" + portalName + "/search"
  val prettyPrinter = new PrettyPrinter(150, 5)
  val params: Map[String, Array[String]] = mapAsScalaMap(request.getParameterMap.asInstanceOf[JMap[String, Array[String]]])

}

class RichSearchAPIServiceFactory {

  @Autowired @BeanProperty var beanQueryModelFactory: BeanQueryModelFactory = _
  @Autowired @BeanProperty var launchProperties: Properties = _
  @Autowired @BeanProperty var queryAnalyzer: QueryAnalyzer = _
  @Autowired @BeanProperty var accessKey: AccessKey = _
  @Autowired @BeanProperty var clickStreamLogger : ClickStreamLogger = _

  def getApiResponse(request: HttpServletRequest, locale: Locale, lookup: LocalizedFieldNames.Lookup, response: HttpServletResponse) : String = {
    val apiRequestObject = ApiRequestObject(request, locale, response, beanQueryModelFactory, launchProperties, queryAnalyzer, accessKey, clickStreamLogger, lookup)
    RichSearchAPIService.processRequest(apiRequestObject)
  }
}

object RichSearchAPIService {

  private val log : Logger = Logger.getLogger("RichSearchAPIService")

  def processRequest(aro: ApiRequestObject) : String = {
    val service = new RichSearchAPIService(aro)
    service.parseRequest
  }

  def renderXMLFields(field : FieldValue, aro: ApiRequestObject) : Seq[Elem] = {
    field.getValueAsArray.map(value =>
      try {
        XML.loadString("<%s>%s</%s>\n".format(field.getKeyAsXml, encodeUrl(value, field, aro), field.getKeyAsXml))
      }
      catch {
        case ex : Exception =>
          log error ("unable to parse " + value + "for field " + field.getKeyAsXml)
          <error/>
      }
    ).toSeq
  }

  def encodeUrl(field: FieldValue, aro: ApiRequestObject): String = {
    if (aro.params.getOrElse("cache", Array[String]("false")).head.contains("true") && field.getKey == "europeana_object")
      aro.cacheUrl + URLEncoder.encode(field.getFirst, "utf-8")
    else field.getFirst
  }

  def encodeUrl(fields: Array[String], label: String, aro: ApiRequestObject): Array[String] = {
    if (aro.params.getOrElse("cache", Array[String]("false")).head.contains("true") && label == "europeana_object")
      fields.map(fieldEntry => aro.cacheUrl + URLEncoder.encode(fieldEntry, "utf-8"))
    else fields
  }

  def encodeUrl(content: String, field: FieldValue, aro: ApiRequestObject): String = {
    if (aro.params.getOrElse("cache", Array[String]("false")).head.contains("true") && field.getKey == "europeana_object")
      aro.cacheUrl + URLEncoder.encode(content, "utf-8")
    else if (content.startsWith("http://")) content.replaceAll("&", "&amp;")
    else content.replaceAll(" & ", "&amp;")
  }
}