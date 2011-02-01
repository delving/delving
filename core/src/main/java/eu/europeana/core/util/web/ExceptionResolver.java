/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.core.util.web;

import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryProblem;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

/**
 * Snap up the exception and send them to a view
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ExceptionResolver implements HandlerExceptionResolver {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private EmailSender emailSender;

    @Value("#{launchProperties['cacheUrl']}")
    private String cacheUrl;

    @Value("#{launchProperties['debug']}")
    private String debug;

    @Value("#{launchProperties['portal.name']}")
    private String portalName;

    @Value("#{launchProperties['portal.theme']}")
    private String portalTheme;

    @Value("#{launchProperties['portal.color']}")
    private String portalColor;

    @Value("#{launchProperties['system.from']}")
    private String fromEmail;

    @Value("#{launchProperties['exception.to']}")
    private String toEmail;

    @Value("#{launchProperties['portal.displayName']}")
    private String portalDisplayName;

    @Resource(name = "includedMacros")
    private List<String> includedMacros;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) {
        if (request.getRequestURI().endsWith(".ajax")) {
            return ajaxFailure(request, response, exception);
        }
        else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            QueryProblem queryProblem = QueryProblem.NONE;
            if (exception instanceof EuropeanaQueryException) {
                queryProblem = ((EuropeanaQueryException) exception).getFetchProblem();
            }
            Boolean debugMode = Boolean.valueOf(debug);
            String stackTrace = getStackTrace(exception);
            if (queryProblem == QueryProblem.NONE || queryProblem == QueryProblem.SOLR_UNREACHABLE) {

                if (!debugMode) { // don't send email in debugMode
                    emailSender.
                            create("exception").
                            setFrom(fromEmail).
                            setTo(toEmail).
                            setSubject(queryProblem.getFragment()).
                            set("hostName", request.getServerName()).
                            set("request", ControllerUtil.formatFullRequestUrl(request)).
                            set("stackTrace", stackTrace).
                            set("cacheUrl", cacheUrl).
                            set("portalName", portalName).
                            set("portalTheme", portalTheme).
                            set("portalColor", portalColor).
                            set("portalDisplayName", portalDisplayName).
                            set("agent", request.getHeader("User-Agent")).
                            set("referer", request.getHeader("referer")).
                            send();
                }
                else {
                    log.error(stackTrace);
                }
            }
            String errorMessage = MessageFormat.format("errorMessage={0}", queryProblem.toString());
            clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.EXCEPTION_CAUGHT, errorMessage);
            ModelAndView mav = new ModelAndView("exception");
            mav.addObject("debug", debugMode);
            mav.addObject("interfaceLanguage", ControllerUtil.getLocale(request));
            mav.addObject("cacheUrl", cacheUrl);
            mav.addObject("portalName", portalName);
            mav.addObject("portalTheme", portalTheme);
            mav.addObject("portalColor", portalColor);
            mav.addObject("portalDisplayName", portalDisplayName);
            mav.addObject("queryProblem", queryProblem);
            mav.addObject("exception", exception);
            mav.addObject("stackTrace", stackTrace);
            mav.addObject("includedMacros", includedMacros);
            return mav;
        }
    }

    private ModelAndView ajaxFailure(HttpServletRequest request, HttpServletResponse response, Exception e) {
        response.setStatus(HttpStatus.SC_NOT_FOUND);
        ModelAndView page = ControllerUtil.createModelAndViewPage("xml/ajax");
        page.addObject("success", false);
        page.addObject("exception", getStackTrace(e));
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.AJAX_ERROR);
        log.warn("Problem handling AJAX request", e);
        return page;
    }

    private static String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
