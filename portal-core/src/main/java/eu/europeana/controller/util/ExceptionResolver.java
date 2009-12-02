/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.controller.util;

import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryProblem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Snap up the exception and send them to a view
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ExceptionResolver implements HandlerExceptionResolver {
    private Logger log = Logger.getLogger(getClass());
    private EmailSender emailSender;
    private String targetEmailAddress;
    private String emailFrom;
    private Map config;

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void setTargetEmailAddress(String targetEmailAddress) {
        this.targetEmailAddress = targetEmailAddress;
    }

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) {
        QueryProblem queryProblem = QueryProblem.NONE;
        // todo: decide if we are going to give an error code on the exeption resolver
        // response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        if (exception instanceof EuropeanaQueryException) {
            queryProblem = ((EuropeanaQueryException) exception).getFetchProblem();
        }
        Boolean debugMode = Boolean.valueOf((String) config.get("debug"));
        String stackTrace = getStackTrace(exception);
        if (queryProblem == QueryProblem.NONE || queryProblem == QueryProblem.SOLR_UNREACHABLE) {
            try {
                Map<String, Object> model = new TreeMap<String, Object>();
                model.put("hostName", request.getServerName());
                model.put("request", request.getRequestURI() + "?" + request.getQueryString());
                model.put("stackTrace", stackTrace);
                model.put("cacheUrl", config.get("cacheUrl"));
                String subject = queryProblem.getFragment();
                emailSender.sendEmail(targetEmailAddress, emailFrom, subject, model);
            }
            catch (Exception e) {
                log.warn("Unable to send email to " + targetEmailAddress, e);
            }
            if (debugMode) {
                log.error(stackTrace);
            }
        }
        ModelAndView mav = new ModelAndView("exception");
        mav.addObject("debug", debugMode);
        mav.addObject("cacheUrl", config.get("cacheUrl"));
        mav.addObject("queryProblem", queryProblem);
        mav.addObject("exception", exception);
        mav.addObject("stackTrace", stackTrace);
        return mav;
    }

    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public void setConfig(Map config) {
        this.config = config;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
}
