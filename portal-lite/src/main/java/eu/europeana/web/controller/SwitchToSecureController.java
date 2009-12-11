package eu.europeana.web.controller;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a controller which just redirects back to the same place without the "secure" part of the URL, which is
 * to be marked as requiring ROLE_USER in the Spring security set up.  This causes a normally insecure page to
 * trigger a security login.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SwitchToSecureController extends AbstractController {
    private Logger logger = Logger.getLogger(getClass());
    private static String SECURE = "secure/";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURL().toString()+"?"+request.getQueryString();
        int securePos = url.indexOf(SECURE);
        if (securePos < 0) {
            throw new IllegalArgumentException("Expected to find '"+SECURE+"' in the request URL");
        }
        String redirect = url.substring(0,securePos)+url.substring(securePos+SECURE.length());
        logger.info("redirecting to: "+redirect);
        return new ModelAndView("redirect:"+redirect);
    }
}