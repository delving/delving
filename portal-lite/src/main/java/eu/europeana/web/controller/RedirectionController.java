package eu.europeana.web.controller;

import eu.europeana.query.RequestLogger;
import eu.europeana.web.util.ControllerUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

/**
 * `
 * The page where you are redirected to the isShownAt and isShownBy links
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class RedirectionController {
    private Logger logger = Logger.getLogger(getClass());
    private static String SHOWN_AT = "shownAt";
    private static String SHOWN_BY = "shownBy";
    private static String PROVIDER = "provider";
    private static String EUROPEANA_ID = "id";

    @Autowired
    private RequestLogger requestLogger;

    public void setRequestLogger(RequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }

    @RequestMapping("/redirect.html")
    protected ModelAndView handleRedirectFromFullView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String isShownAt = request.getParameter(SHOWN_AT);
        String isShownBy = request.getParameter(SHOWN_BY);
        String provider = request.getParameter(PROVIDER);
        String europeanaId = request.getParameter(EUROPEANA_ID);
        String redirect;
        if (isShownAt != null) {
            redirect = isShownAt;
        } else if (isShownBy != null) {
            redirect = isShownBy;
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Expected to find '{0}' or '{1}' in the request URL", SHOWN_AT, SHOWN_BY));
        }
        // todo: implement request logger
        logger.info(MessageFormat.format("redirecting to: {0} for id {1}, by provider {2}", redirect, europeanaId, provider));
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }
}