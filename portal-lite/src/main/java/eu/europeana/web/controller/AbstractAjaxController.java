package eu.europeana.web.controller;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Superclass for all ajax controllers
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public abstract class AbstractAjaxController extends AbstractController {

    protected Logger log = Logger.getLogger(getClass());

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("ajax request");
        response.setContentType("text/xml");
        boolean debug = false;
        boolean success = false;
        String exceptionString = "";
        try {
            if (!hasJavascriptInjection(request)) {
                success = handleAjax(request);
            }
        }
        catch (Exception e) {
            success = false;
            response.setStatus(400);
            exceptionString = getStackTrace(e);
            log.warn("Problem handling AJAX request", e);
        }
        ModelAndView mav = new ModelAndView("ajax");
        mav.addObject("success", String.valueOf(success));
        mav.addObject("exception", exceptionString);
        mav.addObject("debug", debug);
        return mav;
    }

    protected String getStringParameter(String parameterName, HttpServletRequest request) {
        String stringValue = request.getParameter(parameterName);
        if (stringValue == null) {
            throw new IllegalArgumentException("Missing parameter: "+parameterName);
        }
        stringValue = stringValue.trim();
        return stringValue;
    }

    public abstract boolean handleAjax(HttpServletRequest request) throws Exception;

    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private boolean hasJavascriptInjection(HttpServletRequest request) {
        boolean hasJavascript = false;
        Map map = request.getParameterMap();
        for (Object o : map.keySet()) {
            if (request.getParameter(String.valueOf(o)).contains("<")) {
                hasJavascript = true;
                log.warn("The request contains javascript so do not process this request");
                break;
            }
        }
        return hasJavascript;
    }

}