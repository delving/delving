package eu.europeana.core.util.web;

import eu.europeana.core.querymodel.query.FormatType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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