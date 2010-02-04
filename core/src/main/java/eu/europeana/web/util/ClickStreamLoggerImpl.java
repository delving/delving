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

package eu.europeana.web.util;

import eu.europeana.beans.query.BriefBeanView;
import eu.europeana.beans.query.FullBeanView;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.User;
import eu.europeana.query.ClickStreamLogger;
import eu.europeana.query.DocIdWindowPager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.joda.time.DateTime;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ClickStreamLoggerImpl implements ClickStreamLogger {
    private Logger log = Logger.getLogger(getClass());

    @Override
    public void log(HttpServletRequest request, UserAction action, ModelAndView model) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        action, model.getViewName(), printLogAffix(request)));
    }

    /**
     * This method is used the basic information from the <code>HttpServletRequest<code>
     * (@See <code>printLogAffix</code> )
     *
     * @param request the HttpServletRequest from the controller
     * @param action  the UserAction performed in the controller
     */

    @Override
    public void log(HttpServletRequest request, UserAction action) {
        log.info(
                MessageFormat.format(
                        "[action={0}, {1}]",
                        action, printLogAffix(request)));
    }

    @Override
    public void log(HttpServletRequest request, UserAction action, String logString) {
        log.info(
                MessageFormat.format(
                        "[action={0}, {2}, {1}]",
                        action, printLogAffix(request), logString));
    }

    public void log(HttpServletRequest request, StaticPageType pageType) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        UserAction.STATICPAGE, pageType.getViewName(), printLogAffix(request)));
    }

    @Override
    public void log(HttpServletRequest request, Language oldLocale, UserAction languageChange) {
        log.info(
                MessageFormat.format(
                        "[action={0}, oldLang={1}, {2}]",
                        languageChange, oldLocale.toString(), printLogAffix(request)));
    }

    @Override
    public void log(HttpServletRequest request, BriefBeanView briefBeanView, SolrQuery solrQuery, ModelAndView model) {
        ClickStreamLogger.LogTypeId logTypeId;
        String query = briefBeanView.getPagination().getPresentationQuery().getUserSubmittedQuery(); //
        String queryConstraints = "";
        if (solrQuery.getFilterQueries() != null) {
            String[] filterQueries = solrQuery.getFilterQueries(); // a comma separated list of qf's from url. // todo change to CU variant later
            StringBuilder out = new StringBuilder();
            for (String filterQuery : filterQueries) {
                out.append(filterQuery).append(",");
            }
            queryConstraints = out.toString().substring(0, out.toString().length() -1);
        }
//        String pageId;
        // private String state;
        int pageNr = briefBeanView.getPagination().getPageNumber();
        int nrResults = briefBeanView.getPagination().getNumFound();
        String languageFacets = briefBeanView.getFacetLogs().get("LANGUAGE");
        String countryFacet = briefBeanView.getFacetLogs().get("COUNTRY");
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, query={2}, queryType={7}, queryConstraints=\"{3}\", page={4}, " +
                                "numFound={5}, langFacet={8}, countryFacet={9}, {6}]",
                        UserAction.BRIEF_RESULT, model.getViewName(), query,
                        queryConstraints, pageNr, nrResults, printLogAffix(request), solrQuery.getQueryType(),
                        languageFacets, countryFacet));
    }

    @Override
    public void log(HttpServletRequest request, FullBeanView fullResultView, ModelAndView model, String europeanaUri) {
        // todo implement this

        String originalQuery = "";
        String startPage = "";
        try {
            DocIdWindowPager idWindowPager = fullResultView.getDocIdWindowPager();
            originalQuery = idWindowPager.getQuery();
            startPage = idWindowPager.getStartPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(
                MessageFormat.format(
                        "[action={0}, europeana_uri={2}, query={4}, start={3}, {1}]",
                        UserAction.FULL_RESULT, printLogAffix(request), europeanaUri,
                        startPage, originalQuery));
    }

    private String printLogAffix(HttpServletRequest request) {
        DateTime date = new DateTime();
        String ip = request.getRemoteAddr();
        String reqUrl = getRequestUrl(request);
        final User user = ControllerUtil.getUser();
        String userId;
        if (user != null) {
            userId = user.getId().toString();
        }
        else {
            userId = "";
        }
        String language = ControllerUtil.getLocale(request).toString();
        return MessageFormat.format(
                "userId={0}, lang={1}, req={4}, date={2}, ip={3}",
                userId, language, date, ip, reqUrl);
    }

    private String getRequestUrl(HttpServletRequest request) {
        String base = ControllerUtil.getFullServletUrl(request);
        String queryStringParameters = request.getQueryString();
        Map postParameters = request.getParameterMap();
        StringBuilder out = new StringBuilder();
        String queryString;
        out.append(base);
        if (queryStringParameters != null) {
            out.append("?").append(queryStringParameters);
            queryString = out.toString();
        }
        else if (postParameters.size() > 0) {
            out.append("?");
            for (Object entryKey : postParameters.entrySet()) {
                Map.Entry entry = (Map.Entry) entryKey;
                String key = entry.getKey().toString();
                String[] values = (String[]) entry.getValue();
                for (String value : values) {
                    out.append(key).append("=").append(value).append("&");
                }
            }
            queryString = out.toString().substring(0, out.toString().length() - 1);
        }
        else {
            queryString = out.toString();
        }
        return queryString;
    }
}
