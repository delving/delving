package eu.delving.services.search

import javax.servlet.http.HttpServletRequest
import eu.europeana.core.BeanQueryModelFactory
import xml.Elem

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10/12/10 10:37 PM  
 */

class OpenSearchService(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory) {

  def parseRequest() : String = {
     "response"
  }


  def errorResponse(error: String, errorMessage: String) : Elem = {
     <rss version="2.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/">
       <channel>
         <title>title</title>
         <link>link</link>
         <description>description</description>
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

}

object OpenSearchService {
  def parseHttpServletRequest(request: HttpServletRequest, beanQueryModelFactory: BeanQueryModelFactory) : String = {
     val service = new OpenSearchService(request, beanQueryModelFactory)
     service parseRequest
   }
}