package eu.delving.services.search

import javax.servlet.http.HttpServletRequest
import eu.europeana.core.BeanQueryModelFactory
import xml._
import org.apache.solr.client.solrj.SolrQuery
import java.util.{Properties, Map => JMap}
import scala.collection.JavaConversions._
import eu.europeana.core.querymodel.query._
import eu.delving.core.binding.FieldValue

class RichSearchAPIService(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties, queryAnalyzer: QueryAnalyzer) {

  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")
  val portalName = launchProperties.getProperty("portal.name")
  val briefDocSearch = portalBaseUrl + "/" + portalName + "/search"
  val prettyPrinter = new PrettyPrinter(150, 5)


  def getResultsFromSolr : BriefBeanView = {
    val userQuery = request.getParameter("query")
    require(userQuery != null)
    val params = request.getParameterMap.asInstanceOf[JMap[String, Array[String]]]
    val solrQuery : SolrQuery = SolrQueryUtil.createFromQueryParams(params, queryAnalyzer)
    solrQuery.setFields("*,score")
    beanQueryModelFactory.getBriefResultView(solrQuery, solrQuery.getQuery)
  }

  def parseRequest() : String = {
    val response = try {
      getResultResponse(true)
    }
    catch {
      case ex : Exception =>
      errorResponse(errorMessage = ex.getLocalizedMessage)
    }
    prettyPrinter.format(response)
  }

  def errorResponse(title : String = "", link: String = "", description: String = "", error: String = "",
                    errorMessage: String = "") : Elem = {
     <rss version="2.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/">
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

  def renderFields(field : FieldValue) : Seq[Elem] = {
    field.getValueAsArray.map(value =>
      try {
        XML.loadString(format("<%s>%s</%s>\n", field.getKeyAsXml, value, field.getKeyAsXml))
      }
      catch {
        case ex : Exception =>
          println(value)
          <error/>
      }
    ).toSeq
  }

  def renderPageLink(pageLink : PageLink) : Elem = {
    <link start={pageLink.getStart.toString} isLinked={pageLink.isLinked.toString}>{pageLink.getDisplay}</link>
  }

  def renderFacetQueryLinks(fql : FacetQueryLinks) : Elem = {
    <facet name={fql.getType} isSelected={fql.isSelected.toString}>
      {for (link <- fql.getLinks)
        yield <link url={link.getUrl} value={link.getValue} count={link.getCount.toString}>{link.getValue} ({link.getCount})</link>
        }
    </facet>
  }

  def getResultResponse(authorized : Boolean) : Elem = {
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
              {pagination.getBreadcrumbs.map(bc => <breadcrumb field={bc.getField} href={bc.getHref} value={bc.getValue}>{bc.getDisplay}</breadcrumb>)}
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

    if (authorized) response else errorResponse("Error", "Not authorized. Open Search is only available for authorized partners. Please contact info@delving.eu for more info.")
  }
}

object RichSearchAPIService {
  def parseHttpServletRequest(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties, queryAnalyzer: QueryAnalyzer) : String = {
    val service = new RichSearchAPIService(request, beanQueryModelFactory, launchProperties, queryAnalyzer: QueryAnalyzer)
    service parseRequest
  }
}
