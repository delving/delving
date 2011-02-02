package eu.delving.services.search

import eu.europeana.core.BeanQueryModelFactory
import xml._
import org.apache.solr.client.solrj.SolrQuery
import java.util.{Properties, Map => JMap}
import scala.collection.JavaConversions._
import eu.europeana.core.querymodel.query._
import eu.delving.core.binding.FieldValue
import collection.mutable.LinkedHashMap
import javax.servlet.http. {HttpServletResponse, HttpServletRequest}
import net.liftweb.json. {Printer, Extraction}
import net.liftweb.json.JsonAST._
import org.apache.log4j.Logger

class RichSearchAPIService(request: HttpServletRequest, httpResponse: HttpServletResponse,
                           beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties,
                           queryAnalyzer: QueryAnalyzer) {

  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")
  val portalName = launchProperties.getProperty("portal.name")
  val briefDocSearch = portalBaseUrl + "/" + portalName + "/search"
  val prettyPrinter = new PrettyPrinter(150, 5)
  val params = asScalaMap(request.getParameterMap.asInstanceOf[JMap[String, Array[String]]])

  private val log : Logger = Logger.getLogger("RichSearchAPIService")

  def parseRequest() : String = {
    httpResponse setCharacterEncoding ("utf-8")

    val format = params.getOrElse("format", Array[String]("default")).head

    val response = try {
      format match {
        case "json" => getJsonResultResponse()
        case "jsonp" =>
          getJsonResultResponse(params.get("callback").getOrElse(Array[String]("delvingCallback")).head)
        case _ => getXMLResultResponse(true)
      }
    }
    catch {
      case ex : Exception =>
        errorResponse(errorMessage = ex.getLocalizedMessage)
    }
    response
  }

  private def getResultsFromSolr : BriefBeanView = {
    val userQuery = request.getParameter("query")
    require(userQuery != null)
    val solrQuery : SolrQuery = SolrQueryUtil.createFromQueryParams(request.getParameterMap.asInstanceOf[JMap[String, Array[String]]], queryAnalyzer)
    solrQuery.setFields("*,score")
    beanQueryModelFactory.getBriefResultView(solrQuery, solrQuery.getQuery)
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
    def encodeUrl(content : String) : String =
      if (content.startsWith("http://")) content.replaceAll("&", "&amp;") else content


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
        yield <link url={minusAmp(link.getUrl)} value={link.getValue} count={link.getCount.toString}>{link.getValue} ({link.getCount})</link>
        }
    </facet>
  }

  private def minusAmp(link : String) = link.replaceAll("amp;", "")

  def getXMLResultResponse(authorized : Boolean) : String = {
    httpResponse setContentType ("text/xml")

    val briefResult = getResultsFromSolr
    val pagination = briefResult.getPagination
    val searchTerms = pagination.getPresentationQuery.getUserSubmittedQuery
    val startPage = pagination.getStart

    val response : Elem = 
      <results xmlns:icn="http://www.icn.nl/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:dc="http://purl.org/dc/elements/1.1/"
               xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd"
               xmlns:raw="http://delving.eu/namespaces/raw" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ese="http://www.europeana.eu/schemas/ese/"
               xmlns:abm="http://to_be_decided/abm/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
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
        <items>
          {for (item <- briefResult.getBriefDocs) yield
            renderRecord(item)}
        </items>
        <facets>
            {for (facetQuerytLink <- briefResult.getFacetQueryLinks) yield
            renderFacetQueryLinks(facetQuerytLink)}
        </facets>
      </results>

    "<?xml version='1.0' encoding='utf-8' ?>\n" + prettyPrinter.format(response)
  }

  def errorResponse(title : String = "", link: String = "", description: String = "", error: String = "",
                    errorMessage: String = "") : String = {

    httpResponse setStatus (HttpServletResponse.SC_BAD_REQUEST)

     val response = <rss version="2.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/">
       <channel>
         <title>{title}</title>
         <link>{link}</link>
         <description>{description}</description>
         <openSearch:totalResults>1</openSearch:totalResults>
         <openSearch:startIndex>1</openSearch:startIndex>
         <openSearch:itemsPerPage>1</openSearch:itemsPerPage>
         <item>
           <title>{error}</title>
           <description>{errorMessage}</description>
         </item>
       </channel>
     </rss>

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
      val output = getResultsFromSolr.getBriefDocs.map(doc => renderJsonRecord(doc)).toList

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
          if (label.multivalued) {recordMap.put(label.name, fieldValue.getValueAsArray)} else {recordMap.put(label.name, fieldValue.getFirst)}
        }
      }
    )
    recordMap.toMap
  }
}

case class RecordLabel(name : String, fieldValue : String, multivalued : Boolean = false)

object RichSearchAPIService {

  def processRequest(request: HttpServletRequest, response: HttpServletResponse, beanQueryModelFactory: BeanQueryModelFactory,
                     launchProperties: Properties, queryAnalyzer: QueryAnalyzer) : String = {
    val service = new RichSearchAPIService(request, response, beanQueryModelFactory, launchProperties, queryAnalyzer)
    service parseRequest
  }

}