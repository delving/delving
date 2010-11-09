package eu.delving.core.util;

import org.joda.time.DateTime;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 11/2/10 12:47 PM
 */
public class ClickStreamLoggerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startRequestDateTime", new DateTime());
        // Proceed in any case.
        return true;
    }
}
