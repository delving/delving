/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.controller;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.core.querymodel.query.BriefBeanView;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.FullBeanView;
import eu.europeana.core.querymodel.query.QueryModelFactory;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;

/**
 * Annotation-based handling of result pages
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class ResultController {

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    @Autowired
    private ClickStreamLogger clickStreamLogger;
       
    @Value("#{europeanaProperties['image.annotation.tool.base.url']}")
    private String imageAnnotationToolBaseUrl;
    
    @RequestMapping("/full-doc.html")
    @SuppressWarnings("unchecked")
    public ModelAndView fullDocHtml(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "uri", required = true) String uri,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "format", required = false) String format,
            HttpServletRequest request
    ) throws Exception {
        final Map params = request.getParameterMap();
        boolean srwFormat = format != null && format.equals("srw");

        // get results
        final FullBeanView fullResultView = beanQueryModelFactory.getFullResultView(params);

        // create ModelAndView
        ModelAndView page = ControllerUtil.createModelAndViewPage(srwFormat ? "full-doc-srw" : "full-doc");
        page.addObject("result", fullResultView);
        if (fullResultView.getDocIdWindowPager() != null) {
            page.addObject("pagination", fullResultView.getDocIdWindowPager());
        }
        if (format != null && format.equalsIgnoreCase("labels")) {
            page.addObject("format", format);
        }
        page.addObject("uri", uri);
        page.addObject("imageAnnotationToolBaseUrl",imageAnnotationToolBaseUrl);
        clickStreamLogger.logFullResultView(request, fullResultView, page, fullResultView.getFullDoc().getId());
        return page;
    }

    @RequestMapping("/brief-doc.html")
    public ModelAndView briefDocHtml(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "format", required = false) String format,
            HttpServletRequest request
    ) throws EuropeanaQueryException, UnsupportedEncodingException {

        // check if we are dealing with a request from the advanced search page
        @SuppressWarnings("unchecked")
        SolrQuery solrQuery = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        BriefBeanView briefBeanView = beanQueryModelFactory.getBriefResultView(solrQuery, request.getQueryString());

        // Create ModelAndView
        ModelAndView page = ControllerUtil.createModelAndViewPage("brief-doc-window");
        page.addObject("display", format);
        page.addObject("result", briefBeanView);
        page.addObject("query", briefBeanView.getPagination().getPresentationQuery().getUserSubmittedQuery());
        page.addObject("briefDocs", briefBeanView.getBriefDocs());
        page.addObject("queryToSave", briefBeanView.getPagination().getPresentationQuery().getQueryToSave());
        page.addObject("queryStringForPresentation", briefBeanView.getPagination().getPresentationQuery().getQueryForPresentation());
        page.addObject("breadcrumbs", briefBeanView.getPagination().getBreadcrumbs());
        page.addObject("nextQueryFacets", briefBeanView.getFacetQueryLinks());
        page.addObject("pagination", briefBeanView.getPagination());
        page.addObject("queryToSave", briefBeanView.getPagination().getPresentationQuery().getQueryToSave());
        page.addObject("servletUrl", ControllerUtil.getServletUrl(request));
        clickStreamLogger.logBriefResultView(request, briefBeanView, solrQuery, page);
        return page;
    }

//    <prop key="/brief-doc.rss">briefDocController</prop>
//    <prop key="/brief-doc.rdf">briefDocController</prop>
//    <prop key="/brief-doc.srw">briefDocController</prop>

    /*
     * The page where you are redirected to the isShownAt and isShownBy links
     */

    @RequestMapping("/redirect.html")
    public ModelAndView handleRedirectFromFullView(HttpServletRequest request) throws Exception {
        String SHOWN_AT = "shownAt";
        String SHOWN_BY = "shownBy";
        String PROVIDER = "provider";
        String EUROPEANA_ID = "id";

        String isShownAt = request.getParameter(SHOWN_AT);
        String isShownBy = request.getParameter(SHOWN_BY);
        String provider = request.getParameter(PROVIDER);
        String europeanaId = request.getParameter(EUROPEANA_ID);
        String redirectLink;
        if (isShownAt != null) {
            redirectLink = isShownAt;
        } else if (isShownBy != null) {
            redirectLink = isShownBy;
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Expected to find '{0}' or '{1}' in the request URL", SHOWN_AT, SHOWN_BY));
        }
        String logString = MessageFormat.format("outlink={0}, provider={2}, europeana_id={1}", redirectLink, europeanaId, provider);
        clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.REDIRECT_OUTLINK, logString);
        return ControllerUtil.createModelAndViewPage("redirect:" + redirectLink);
    }
}
