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

import eu.delving.core.binding.FacetStatisticsMap;
import eu.delving.core.binding.SolrBindingService;
import eu.europeana.core.querymodel.query.*;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("#{launchProperties['image.annotation.tool.base.url']}")
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
        ModelAndView page = ControllerUtil.createModelAndViewPage(srwFormat ? "xml/full-doc-srw" : "full-doc");
        page.addObject("result", fullResultView);
        if (fullResultView.getDocIdWindowPager() != null) {
            page.addObject("pagination", fullResultView.getDocIdWindowPager());
        }
        if (format != null && format.equalsIgnoreCase("labels")) {
            page.addObject("format", format);
        }
        page.addObject("uri", uri);
        page.addObject("imageAnnotationToolBaseUrl", imageAnnotationToolBaseUrl);
        clickStreamLogger.logFullResultView(request, fullResultView, page, fullResultView.getFullDoc().getId());
        return page;
    }

    @RequestMapping("/record/{collId}/{recordHash}.html")
    @SuppressWarnings("unchecked")
    public ModelAndView fullDocRest(
            @PathVariable String collId, @PathVariable String recordHash,
            @RequestParam(value = "format", required = false) String format,
            HttpServletRequest request
    ) throws Exception {
        Map params = request.getParameterMap();
        boolean srwFormat = format != null && format.equalsIgnoreCase("srw");

        String uri = collId + "/" + recordHash;
        Map<String, String[]> fullParams = new HashMap<String, String[]>();
        fullParams.putAll((Map<? extends String,? extends String[]>) params);
        fullParams.put("uri", new String[]{uri});

        // get results
        final FullBeanView fullResultView = beanQueryModelFactory.getFullResultView(fullParams);

        // create ModelAndView
        ModelAndView page = ControllerUtil.createModelAndViewPage(srwFormat ? "xml/full-doc-srw" : "full-doc");
        page.addObject("result", fullResultView);
        if (fullResultView.getDocIdWindowPager() != null) {
            page.addObject("pagination", fullResultView.getDocIdWindowPager());
        }
        if (format != null && format.equalsIgnoreCase("labels")) {
            page.addObject("format", format);
        }
        page.addObject("uri", uri);
        page.addObject("imageAnnotationToolBaseUrl", imageAnnotationToolBaseUrl);
        clickStreamLogger.logFullResultView(request, fullResultView, page, fullResultView.getFullDoc().getId());
        return page;
    }


    @RequestMapping("/statistics.html")
    public ModelAndView statisticsHtml(
            @RequestParam(value = "query", required = false) String query,
            HttpServletRequest request
    ) throws EuropeanaQueryException, UnsupportedEncodingException {
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameterMap = (Map<String, String[]>) request.getParameterMap();
        if (parameterMap.isEmpty()) {
            parameterMap = new HashMap<String, String[]>();
            parameterMap.put("query", new String[]{"*:*"});
        }
        SolrQuery solrQuery = beanQueryModelFactory.createFromQueryParams(parameterMap);
        solrQuery.setFacet(true);
        solrQuery.setFacetMinCount(1);
        solrQuery.setFacetLimit(300);
        solrQuery.addFacetField("MUNICIPALITY", "PROVIDER", "DATAPROVIDER", "COUNTY");
        solrQuery.setRows(0);
        final QueryResponse solrResponse = beanQueryModelFactory.getSolrResponse(solrQuery);
        final List<FacetField> facetFields = solrResponse.getFacetFields();

        // Create ModelAndView
        ModelAndView page = ControllerUtil.createModelAndViewPage("statistics");
        final FacetStatisticsMap facetStatistics = SolrBindingService.createFacetStatistics(facetFields);
        page.addObject("facetMap", facetStatistics);
        return page;
    }

    private String getViewName(String format) {
        String viewName = "brief-doc-window";
        if (format != null) {
            if (format.equalsIgnoreCase("srw")) {
                viewName = "xml/brief-doc-window-srw";
            }
            else if (format.equalsIgnoreCase("rss")) {
                viewName = "xml/brief-doc-window-rss";
            }
            else if (format.equalsIgnoreCase("rdf")) {
                viewName = "xml/brief-doc-window-rdf";
            }
        }
        return viewName;
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
        ModelAndView page = ControllerUtil.createModelAndViewPage(getViewName(format));
        page.addObject("display", format);
        page.addObject("ramdomSortKey", SolrQueryUtil.createRandomSortKey());
        page.addObject("result", briefBeanView);
        page.addObject("query", briefBeanView.getPagination().getPresentationQuery().getUserSubmittedQuery());
        page.addObject("briefDocs", briefBeanView.getBriefDocs());
        page.addObject("queryToSave", briefBeanView.getPagination().getPresentationQuery().getQueryToSave());
        page.addObject("queryStringForPresentation", briefBeanView.getPagination().getPresentationQuery().getQueryForPresentation());
        page.addObject("breadcrumbs", briefBeanView.getPagination().getBreadcrumbs());
        page.addObject("nextQueryFacets", briefBeanView.getFacetQueryLinks());
        page.addObject("pagination", briefBeanView.getPagination());
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

    @SuppressWarnings({"unchecked"})
    @RequestMapping("/comparator.html")
    public ModelAndView searchComparator(HttpServletRequest request) throws EuropeanaQueryException, UnsupportedEncodingException {

        // simple search (default)
        SolrQuery defaultQuery = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        defaultQuery.setIncludeScore(true);
        defaultQuery.setShowDebugInfo(true);
        BriefBeanView defaultBriefBeanView = beanQueryModelFactory.getBriefResultView(defaultQuery, request.getQueryString());

        // advanced search (default)
        SolrQuery standardQuery = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        standardQuery.setIncludeScore(true);
        standardQuery.setShowDebugInfo(true);
        standardQuery.setQueryType(QueryType.ADVANCED_QUERY.toString());
        BriefBeanView standardViewBriefBeanView = beanQueryModelFactory.getBriefResultView(standardQuery, request.getQueryString());

        // custom search 1
        SolrQuery custom1Query = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        custom1Query.setIncludeScore(true);
        custom1Query.setShowDebugInfo(true);
        if (!custom1Query.getQueryType().equalsIgnoreCase(QueryType.ADVANCED_QUERY.toString())) {
            custom1Query.setQueryType("clean_dismax");
            custom1Query.setParam("q.alt", "*:*"); // alternative query when q is empty
            custom1Query.setParam("qf", "title^1.1"); // query fields on which the search is performed
            custom1Query.setParam("mm", "2<-1 5<-2 6<80%");
            custom1Query.setParam("pf", "text^0.8 title^1.5 creator^1.5 dc_subject dc_description");
            custom1Query.setParam("ps", "100");
            custom1Query.setParam("tie", "0.01");
            custom1Query.setParam("fl", "*, score"); //default field names to be returned
            custom1Query.setParam("bf", "ord(popularity)^0.5"); // key to search configuration
        }
        BriefBeanView custom1BriefBeanView = beanQueryModelFactory.getBriefResultView(custom1Query, request.getQueryString());


        // custom search 2
        SolrQuery custom2Query = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        custom2Query.setIncludeScore(true);
        custom2Query.setShowDebugInfo(true);
        if (!custom2Query.getQueryType().equalsIgnoreCase(QueryType.ADVANCED_QUERY.toString())) {
            custom2Query.setQueryType("clean_dismax");
            custom2Query.setParam("q.alt", "*:*"); // alternative query when q is empty
            custom2Query.setParam("qf", "dc_title dc_subject dc_creator dc_description"); // query fields on which the search is performed
            custom2Query.setParam("mm", "2<-1 5<-2 6<90%");
            custom2Query.setParam("pf", "text^0.8 title^1.5 creator^1.5 dc_subject dc_description");
            custom2Query.setParam("ps", "100");
            custom2Query.setParam("tie", "0.01");
            custom2Query.setParam("fl", "*, score"); //default field names to be returned
            custom2Query.setParam("bf", ""); // key to search configuration
        }
        BriefBeanView custom2BriefBeanView = beanQueryModelFactory.getBriefResultView(custom2Query, request.getQueryString());


        ModelAndView comparator = ControllerUtil.createModelAndViewPage("search-comparator");
        comparator.addObject("defaultView", defaultBriefBeanView);
        comparator.addObject("standardView", standardViewBriefBeanView);
        comparator.addObject("custom1View", custom1BriefBeanView);
        comparator.addObject("custom2View", custom2BriefBeanView);

        return comparator;
    }

    /*
    * freemarker Template not loadable from database
    */

    @RequestMapping("/error.html")
    public ModelAndView errorPageHandler(HttpServletRequest request) {
//        clickStreamLogger.logCustomUserAction(request, "error");
        return ControllerUtil.createModelAndViewPage("error");
    }
}
