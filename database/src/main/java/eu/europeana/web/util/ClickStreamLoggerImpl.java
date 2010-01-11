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

import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.User;
import eu.europeana.query.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ClickStreamLoggerImpl implements ClickStreamLogger {
    private Logger log = Logger.getLogger(getClass());

    private ClickStreamLogger.LogTypeId logTypeId;
    private String query; //
    private QueryExpression.QueryType queryType;
    private String queryConstraints; // a comma separated list of qf's from url.
    private String pageId;
    // private String state;
    private int pageNr;
    private int nrResults;
    private String languageFacets;
    private String countryFacet;
    private UserAction action;


    /*
    * this method is used to log actions in Interceptors
     */

    public void log(HttpServletRequest request, HttpServletResponse servletResponse, UserAction action) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void log(HttpServletRequest request, UserAction action, ModelAndView model) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        action, model.getViewName(), printLogAffix(request)));
    }

    public void log(HttpServletRequest request, ResultModel resultModel, QueryModel queryModel, ResultPagination resultPagination, ModelAndView model, UserAction action) {
        // get elements from queryModel
        nrResults = resultPagination.getNumFound();
        query = queryModel.getQueryString();
        queryType = queryModel.getQueryType();
        queryConstraints = formatQueryConstraints(queryModel.getConstraints());
    }

    public void log(HttpServletRequest request, UserAction action) {
        log.info(
                MessageFormat.format(
                        "[action={0}, {1}]",
                        action, printLogAffix(request)));
    }

    // used for logging from staticPages
    public void log(HttpServletRequest request, StaticPageType pageType) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        UserAction.STATICPAGE, pageType.getViewName(), printLogAffix(request)));
    }


    private String printLogAffix(HttpServletRequest request) {
        DateTime date = new DateTime();
        String ip = request.getRemoteAddr();
        String reqUrl = ControllerUtil.getServletUrl(request);
        final User user = ControllerUtil.getUser();
        String userId;
        if (user != null) {
            userId = user.getId().toString();
        } else {
            userId = "";
        }
        String language = ControllerUtil.getLocale(request).toString();
        return MessageFormat.format(
                "userId={0}, lang={1}, date={2}, ip={3}, req={4}",
                userId, language, date, ip, reqUrl);
    }

    // todo: format full request url
    private String getRequestUrl(HttpServletRequest request) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    // todo: format QueryConstraints
    private String formatQueryConstraints(QueryModel.Constraints constraints) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
