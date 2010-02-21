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

package eu.europeana.web.controller.annotated;

import eu.europeana.core.BeanQueryModelFactory;
import eu.europeana.core.database.StaticInfoDao;
import eu.europeana.core.database.domain.StaticPageType;
import eu.europeana.core.querymodel.query.BriefBeanView;
import eu.europeana.core.querymodel.query.QueryType;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Genereric controller for Browsing Pages.
 * <p/>
 * todo clean up this class
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class BrowsePageController {

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    private static final String[] DEFAULT_YEAR_QUERIES = new String[]{
            "YEAR:1915", "YEAR:1980", "YEAR:2000", "YEAR:1977", "YEAR:1945",
    };
    private static final String[] DEFAULT_TAG_QUERIES = new String[]{
            "USERTAGS:\"moyen age\"", "USERTAGS:\"moulage\"",
    };

    @Autowired
    private BeanQueryModelFactory beanQueryModelFactory;


    @RequestMapping("/bob.html")
    public ModelAndView bobController(HttpServletRequest request) throws Exception {
        ModelAndView model = ControllerUtil.createModelAndViewPage("bob-new");
        String queryString = request.getParameter("query");
        String query = null;
        String queryType = request.getParameter("qt");
        if (queryString != null) {
            query = queryString;
        } else if (queryType != null) {
            if (queryType.equalsIgnoreCase("year")) {
                query = DEFAULT_YEAR_QUERIES[(int) (Math.random() * DEFAULT_YEAR_QUERIES.length)];
            } else if (queryType.equalsIgnoreCase("tags")) {
                query = DEFAULT_TAG_QUERIES[(int) (Math.random() * DEFAULT_TAG_QUERIES.length)];
            }
        } else {
            query = DEFAULT_YEAR_QUERIES[(int) (Math.random() * DEFAULT_YEAR_QUERIES.length)];
        }
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(query)
                .setQueryType(QueryType.ADVANCED_QUERY.toString())
                .setRows(20);
        String startParam = request.getParameter("start");
        int startRow = 0;
        if (startParam != null) {
            if (startParam.contains(",")) {
                startParam = startParam.replaceAll(",", "");
            }
            startRow = Integer.parseInt(startParam);
        }
        model.addObject("startPage", startRow);
        solrQuery.setStart(startRow);
        final BriefBeanView resultView = beanQueryModelFactory.getBriefResultView(solrQuery, request.getQueryString());
        model.addObject("query", query);
        model.addObject("docList", resultView.getBriefDocs());
        model.addObject("pagination", resultView.getPagination());
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.BROWSE_BOB, model);
        return model;
    }

    @RequestMapping("/year-grid.html")
    public ModelAndView yearGridHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.YEAR_GRID;
        ModelAndView model = ControllerUtil.createModelAndViewPage(pageType.getViewName());
        String queryString = request.getParameter("query");
        String query;
        if (queryString != null && !queryString.equalsIgnoreCase("")) {
            query = queryString;
        } else {
            query = "*:*";
        }
        String startParam = request.getParameter("startParam");
        int start = 1;
        if (startParam != null) {
            if (startParam.contains(",")) {
                startParam = startParam.replaceAll(",", "");
            }
            start = Integer.parseInt(startParam);
        }
        String bobQuery = request.getParameter("bq");
        if (bobQuery != null && !bobQuery.equalsIgnoreCase("")) {
            model.addObject("bobQuery", bobQuery);
        }
        model.addObject("query", query);
        model.addObject("start", start);
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(query)
                .setRows(0)
                .setFacet(true)
                .addFacetField("YEAR")
                .setFacetLimit(100)
                .setFacetMinCount(1)
                .setQueryType(QueryType.ADVANCED_QUERY.toString());
        final QueryResponse response = beanQueryModelFactory.getSolrResponse(solrQuery);
        final List<FacetField> list = response.getFacetFields();
        model.addObject("facetList", list);
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.YEAR_GRID, model);
        return model;
    }
}