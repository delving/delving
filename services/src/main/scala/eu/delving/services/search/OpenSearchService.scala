package eu.delving.services.search

import javax.servlet.http.HttpServletRequest
import eu.europeana.core.BeanQueryModelFactory
import xml.Elem
import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.SolrQuery
import java.util.Properties
import eu.europeana.core.querymodel.query. {QueryType, BriefDoc, BriefBeanView}
import eu.delving.core.binding.BriefDocItem

class OpenSearchService(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties) {

  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")
  val portalName = launchProperties.getProperty("portal.name")
  val briefDocSearch = portalBaseUrl + "/" + portalName + "/brief-doc.html"


  def getResultsFromSolr : BriefBeanView = {
    val query : SolrQuery = new SolrQuery(request.getParameter("query"))
    query.setQueryType(QueryType.ADVANCED_QUERY.toString)
    if (request.getParameter("startPage") != null) { query.setStart(request.getParameter("startPage").toInt) }
    beanQueryModelFactory.getBriefResultView(query, query.getQuery)
  }

  def parseRequest() : String = getResultResponse(true).toString

  private def renderRecord(doc : BriefDoc) : Elem = {
    val response =
      <item>
        <title>{doc.getTitle}</title>
        <link>{portalBaseUrl}{doc.getFullDocUrl}?format=srw</link>
        <description>{doc.getFieldValue("dc_description").getFirst}</description>
         <enclosure url= {doc.getThumbnail}/>
      </item>
    response
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

  def getResultResponse(authorized: Boolean) : Elem = {
    val briefResult = getResultsFromSolr
    val pagination = briefResult.getPagination
    val searchTerms = pagination.getPresentationQuery.getUserSubmittedQuery
    val itemsPerPage = 12 // maybe inject this later
    val startPage = pagination.getStart

    val response =
    <rss version="2.0"
      xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/"
      xmlns:atom="http://www.w3.org/2005/Atom">
   <channel>
     <title>Delving Open Search result</title>
     <link>{servicesUrl}/api/open-search?searchTerms={searchTerms}&amp;startPage={startPage}</link>
     <description>{searchTerms} - Delving Open Search</description>
     <opensearch:totalResults>{pagination.getNumFound}</opensearch:totalResults>
     <opensearch:startIndex>{startPage}</opensearch:startIndex>
     <opensearch:itemsPerPage>{pagination.getRows}</opensearch:itemsPerPage>
     <atom:link rel="search" type="application/opensearchdescription+xml" href={servicesUrl +"api/open-search.xml"} title="Delving Search"/>
     <opensearch:Query searchTerms={searchTerms} startPage={startPage.toString} role="request" />
     {for (doc <- briefResult.getBriefDocs) yield
          renderRecord(doc)
     }
   </channel>
 </rss>

     if (authorized) response else errorResponse("Error", "Not authorized. Open Search is only available for authorized partners. Please contact info@delving.eu for more info.")
  }

  def getDescriptionDocument(authorized: Boolean) : Elem = {
    val searchTerms = if (request.getParameter("query") != null) request.getParameter("query") else ""
    val startPage = if (request.getParameter("startPage") != null) request.getParameter("startPage") else ""

    val amp : String = "&amp;"
    val url : Elem = if (authorized) { <Url type="text/html" rel="results" template={servicesUrl + "api/open-search?query=" + searchTerms + "&startPage=" + startPage}/> }
      else {<Url type="application/rss+xml" rel="results" template={briefDocSearch + "?query=" + searchTerms}/>}

  val response =
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
  <ShortName>Delving Search</ShortName>
  <Description>Open search access to Delving.eu</Description>
  <InputEncoding>UTF-8</InputEncoding>
  <Image height="16" width="16" type="image/x-icon">{servicesUrl}favicon.ico</Image>
  {url}
</OpenSearchDescription>
    response
  }

}

object OpenSearchService {
  def parseHttpServletRequest(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties): String = {
    val service = new OpenSearchService(request, beanQueryModelFactory, launchProperties)
    service parseRequest
  }

  def renderDescriptionDocument(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties): String = {
    val service = new OpenSearchService(request, beanQueryModelFactory, launchProperties)
    service.getDescriptionDocument(authorized = true).toString
  }
}