package eu.europeana.controller;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

/**          `
 * The page where you are redirected to the isShownAt and isShownBy links
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class RedirectionController extends AbstractController {
    private Logger logger = Logger.getLogger(getClass());
    private static String SHOWN_AT = "shownAt";
    private static String SHOWN_BY = "shownBy";
    private static String PROVIDER = "provider";
    private static String EUROPEANA_ID = "id";


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String redirect;
        String isShownAt = request.getParameter(SHOWN_AT);
        String isShownBy = request.getParameter(SHOWN_BY);
        String provider = request.getParameter(PROVIDER);
        String europeanaId = request.getParameter(EUROPEANA_ID);
        if (isShownAt != null) {
            redirect = isShownAt;
        }
        else if (isShownBy != null) {
            redirect = isShownBy;
        }
        else {
            throw new IllegalArgumentException(MessageFormat.format("Expected to find '{0}' or '{1}' in the request URL", SHOWN_AT, SHOWN_BY));
        }
        logger.info(MessageFormat.format("redirecting to: {0} for id {1}, by provider {2}", redirect, europeanaId, provider));
//        response.sendRedirect(redirect);
//        return null;
        return new ModelAndView("redirect:"+redirect);
    }
}