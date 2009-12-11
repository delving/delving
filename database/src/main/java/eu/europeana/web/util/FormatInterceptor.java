package eu.europeana.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FormatInterceptor extends HandlerInterceptorAdapter {
    private static final String FORMAT_PARAMETER = "format";

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String extension = uri.substring(uri.lastIndexOf(".")+1);
        request.setAttribute(FORMAT_PARAMETER, FormatType.get(extension));
        return true;
    }
}