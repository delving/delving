package eu.delving.services.search

import javax.servlet.http.HttpServletRequest
import eu.europeana.core.BeanQueryModelFactory
import xml.Elem
import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.SolrQuery
import java.util.Properties
import eu.europeana.core.querymodel.query. {QueryType, BriefDoc, BriefBeanView}

class OpenSearchService(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory, launchProperties: Properties) {

  val servicesUrl = launchProperties.getProperty("services.url")
  val portalBaseUrl = launchProperties.getProperty("portal.baseUrl")


  def getResultsFromSolr : BriefBeanView = {
    val query = new SolrQuery(request.getParameter("searchTerms"))
    query.setQueryType(QueryType.ADVANCED_QUERY.toString)
    if (request.getParameter("startPage") != null) query.setStart(request.getParameter("startPage").toInt)
    beanQueryModelFactory.getBriefResultView(query, query.getQuery)
  }

  def parseRequest() : String = getResultResponse(true).toString

  private def renderRecord(doc : BriefDoc) : Elem = {
    <item>
        <title>${doc.getTitle}</title>
        <link>${portalBaseUrl}${doc.getFullDocUrl}?format=srw</link>
        <description></description>
        <enclosure url='${doc.getThumbnail}'/>
     </item>
}


  def errorResponse(title : String = "", link: String = "", description: String = "", error: String = "",
                    errorMessage: String = "") : Elem = {
     <rss version="2.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/">
       <channel>
         <title>${title}</title>
         <link>${link}</link>
         <description>${description}</description>
         <openSearch:totalResults>1</openSearch:totalResults>
         <openSearch:startIndex>1</openSearch:startIndex>
         <openSearch:itemsPerPage>1</openSearch:itemsPerPage>
         <item>
           <title>${error}</title>
           <description>${errorMessage}</description>
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
     <link>${servicesUrl}?searchTerms=${searchTerms}&amp;startPage=${startPage}</link>
     <description>${searchTerms} - Delving Open Search</description>
     <opensearch:totalResults>${pagination.getNumFound}</opensearch:totalResults>
     <opensearch:startIndex>${startPage}</opensearch:startIndex>
     <opensearch:itemsPerPage>${pagination.getRows}</opensearch:itemsPerPage>
     <atom:link rel="search" type="application/opensearchdescription+xml" href={servicesUrl +"/api/open-search.xml"} title="Delving Search"/>
     <opensearch:Query role="request" searchTerms="${searchTerms}" startPage="${startPage}" />
     {for (doc <- briefResult.getBriefDocs) yield renderRecord(doc)}
   </channel>
 </rss>

     if (authorized) response else errorResponse("Error", "Not authorized. Open Search is only available for authorized partners. Please contact info@delving.eu for more info.")
  }

  def getDescriptionDocument(authorized: Boolean) : Elem = {
    // todo add & to template string
    val amp : String = "&amp;"
    val url : Elem = if (authorized) { <Url type="text/html" rel="results" template="${servicesUrl}/api/open-search?query=${searchTerms}${amp}startPage=${startPage}"/> }
      else {<Url type="application/rss+xml" rel="results" template="${apiUrl}?query={searchTerms}"/>}

  val response =
  <OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
    <ShortName>Delving Search</ShortName>
    <Description>Open search access to Delving.eu</Description>
    <InputEncoding>UTF-8</InputEncoding>
    <Image height="16" width="16" type="image/x-icon">${servicesUrl}/favicon.ico</Image>
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
    val descriptionDocument = service.getDescriptionDocument(true)
    descriptionDocument.toString
  }
}