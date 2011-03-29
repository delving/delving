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

package eu.delving.services.controller;

import eu.delving.core.util.ThemeInterceptor;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
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
        HttpMethod method = new GetMethod(String.format("%s/select?%s", ThemeInterceptor.getTheme().getSolrSelectUrl(), solrQueryString));
        httpClient.executeMethod(method);
        for (Header header : method.getRequestHeaders()) {
            if (header.getName().equalsIgnoreCase("content-type")) {
                response.setContentType(method.getResponseHeader("Content-Type").getValue());
            }
            else if (header.getName().equalsIgnoreCase("server")) {
                //ignore
            }
            else {
                response.setHeader(header.getName(), header.getValue());
            }
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(method.getResponseBodyAsString()); //todo add response from SolrProxy here
        response.getWriter().close();
    }
}
