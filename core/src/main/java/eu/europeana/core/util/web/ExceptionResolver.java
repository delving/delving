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

package eu.europeana.core.util.web;

import eu.delving.core.binding.FreemarkerUtil;
import eu.delving.core.binding.QueryParamList;
import eu.delving.core.util.EmailTarget;
import eu.delving.core.util.PortalTheme;
import eu.delving.core.util.ThemeHandler;
import eu.delving.core.util.ThemeInterceptor;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryProblem;
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

    @Autowired
    private FreemarkerUtil freeMarkerUtil;

    @Resource(name = "includedMacros")
    private List<String> includedMacros;

    @Autowired
    private ThemeHandler themeHandler;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) {
        PortalTheme theme = ThemeInterceptor.getTheme();
        if (theme == null) {
            theme = themeHandler.getDefaultTheme();
        }
        final EmailTarget emailTarget = theme.getEmailTarget();
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
                            setFrom(emailTarget.getSystemFrom()).
                            setTo(emailTarget.getExceptionTo()).
                            setSubject(queryProblem.getFragment()).
                            set("hostName", request.getServerName()).
                            set("request", ControllerUtil.formatFullRequestUrl(request)).
                            set("stackTrace", stackTrace).
                            set("cacheUrl", cacheUrl).
                            set("portalName", portalName).
                            set("portalTheme", theme.getName()).
                            set("portalColor", theme.getColorScheme()).
                            set("portalDisplayName", theme.getDisplayName()).
                            set("agent", request.getHeader("User-Agent")).
                            set("referer", request.getHeader("referer")).
                            send();
                }
                log.error(stackTrace + queryProblem);
            }
            else {
                log.warn("Oops", exception);
            }
            String errorMessage = MessageFormat.format("errorMessage={0}", queryProblem.toString());
            clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.EXCEPTION_CAUGHT, errorMessage);
            ModelAndView mav = ThemeInterceptor.createThemedModelAndViewPage("exception");
            mav.addObject("debug", debugMode);
            mav.addObject("interfaceLanguage", ControllerUtil.getLocale(request));
            mav.addObject("cacheUrl", theme.getCacheUrl());
            mav.addObject("portalName", portalName);
            mav.addObject("portalTheme", "theme/" + theme.getName());
            mav.addObject("portalThemeName", theme.getName());
            mav.addObject("portalColor", theme.getColorScheme());
            mav.addObject("portalDisplayName", theme.getDisplayName());
            mav.addObject("queryProblem", queryProblem);
            mav.addObject("exception", exception);
            mav.addObject("stackTrace", stackTrace);
            mav.addObject("includedMacros", includedMacros);
            final QueryParamList queryParamList = FreemarkerUtil.createQueryParamList(request.getParameterMap());
            mav.addObject("queryParamList", queryParamList);
            mav.addObject("defaultParams", queryParamList.getDefaultParamsFormatted());
            mav.addObject("pageGrabber", freeMarkerUtil);
            return mav;
        }
    }

    private ModelAndView ajaxFailure(HttpServletRequest request, HttpServletResponse response, Exception e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        ModelAndView page = ThemeInterceptor.createThemedModelAndViewPage("xml/ajax");
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
