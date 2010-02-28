package eu.europeana.core.util.web;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Make sure all returns are UTF8
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class Utf8OnlyFilter extends OncePerRequestFilter {
    private static final String UTF8 = "UTF-8";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setContentType("text/html; charset=" + UTF8);
        request.setCharacterEncoding(UTF8);
        response.setCharacterEncoding(UTF8);
        filterChain.doFilter(request, response);
    }
}
