package eu.delving.services.controller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 15, 2010 12:17:29 AM
 */
@Controller
public class SolrProxyController {

    private Logger log = Logger.getLogger(getClass());

    private CommonsHttpSolrServer solrServer;

    public void setSolrServer(CommonsHttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Value("#{launchProperties['solr.selectUrl']}")
    private String solrSelectUrl;

    @Autowired
    private HttpClient httpClient;

    @RequestMapping("/api/solr/select")
    public void searchController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String solrQueryString = request.getQueryString();
        response.sendRedirect(solrSelectUrl + "/select?" + solrQueryString);
//        HttpMethod method = new GetMethod(String.format("%s/select?%s", solrSelectUrl, solrQueryString));
//        httpClient.executeMethod(method);
//        for (Header header : method.getRequestHeaders()) {
//            if (header.getName().equalsIgnoreCase("content-type")) {
//                response.setContentType(method.getResponseHeader("Content-Type").getValue());
//            }
//            else if (header.getName().equalsIgnoreCase("server")) {
//                //ignore
//            }
//            else {
//                response.setHeader(header.getName(), header.getValue());
//            }
//        }
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write(method.getResponseBody())); //todo add response from SolrProxy here
//        response.getWriter().close();
    }
}
