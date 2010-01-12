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

import eu.europeana.beans.views.BriefBeanView;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.query.*;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.DocIdWindowPagerImpl;
import eu.europeana.web.util.QueryConstraints;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

/**
 * Annotation-based handling of result pages
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Controller
public class ResultController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private QueryModelFactory queryModelFactory;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private NewQueryModelFactory beanQueryModelFactory;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping("/full-doc.html")
    public ModelAndView fullDocHtml(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "uri", required = false) String uri,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "format", required = false) String format,
            HttpServletRequest request
    ) throws EuropeanaQueryException, UnsupportedEncodingException {
        boolean srwFormat = format != null && format.equals("srw");
        ModelAndView page = ControllerUtil.createModelAndViewPage(srwFormat ? "full-doc-srw" : "full-doc");
        // todo: setContentType("text/xml;charset=UTF-8"); for srw format
        if (uri == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_URL.toString()); // Expected uri query parameter
        }
        QueryModel queryModel = queryModelFactory.createQueryModel();
        if (query != null && start != null) {
            page.addObject("pagination", new DocIdWindowPagerImpl(uri, request, queryModel));
        }
        queryModel.setResponseType(ResponseType.SINGLE_FULL_DOC);
        queryModel.setStartRow(0);
        queryModel.setQueryExpression(new UriExpression(uri));
        ResultModel resultModel = queryModel.fetchResult();
        if (resultModel.isMissingFullDoc()) {
            EuropeanaId id = dashboardDao.fetchEuropeanaId(uri);
            if (id != null && id.isOrphan()) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_REVOKED.toString());
            }
            else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_INDEXED.toString());
            }
            else {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
            }
        }
        page.addObject("result", resultModel);
        page.addObject("uri", uri);
        if (format != null && format.equalsIgnoreCase("labels")) {
            page.addObject("format", format);
        }
        return page;
    }

    @RequestMapping("/brief-doc.html")
    public ModelAndView briefDocHtml(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "format", required = false) String format,
            HttpServletRequest request
    ) throws JSONException, EuropeanaQueryException, UnsupportedEncodingException {
        ModelAndView page = ControllerUtil.createModelAndViewPage("brief-doc-window");
        SolrQuery solrQuery = beanQueryModelFactory.createFromQueryParams(request.getParameterMap());
        BriefBeanView resultView = beanQueryModelFactory.getBriefResultView(solrQuery, request.getQueryString());
        List<? extends BriefDoc> list = resultView.getBriefDocs();
        page.addObject(resultView);
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
        String redirect;
        if (isShownAt != null) {
            redirect = isShownAt;
        }
        else if (isShownBy != null) {
            redirect = isShownBy;
        }
        else {
            throw new IllegalArgumentException(MessageFormat.format("Expected to find '{0}' or '{1}' in the request URL", SHOWN_AT, SHOWN_BY));
        }
        // todo: implement request log
        log.info(MessageFormat.format("redirecting to: {0} for id {1}, by provider {2}", redirect, europeanaId, provider));
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }

    private String createQueryStringForPresentation(String query, QueryConstraints queryConstraints) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        queryString.append("query").append("=").append(URLEncoder.encode(query, "utf-8"));
        queryString.append(queryConstraints.toQueryString());
        return queryString.toString();
    }

    private class UriExpression implements QueryExpression {
        private String uri;

        private UriExpression(String uri) {
            this.uri = uri;
        }

        public String getQueryString() {
            return getBackendQueryString();
        }

        public String getBackendQueryString() {
            return RecordField.EUROPEANA_URI.toFieldNameString() + ":\"" + uri + "\"";
        }

        public QueryType getType() {
            return QueryType.MORE_LIKE_THIS_QUERY;
        }

        public boolean isMoreLikeThis() {
            return true;
        }
    }

}
