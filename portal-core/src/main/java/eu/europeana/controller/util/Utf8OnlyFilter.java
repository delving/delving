package eu.europeana.controller.util;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Make sure all returns are UTF8
 *
 * @author Yoann, from TEL
 */

public class Utf8OnlyFilter extends OncePerRequestFilter {
    private static final String UTF8 = "UTF-8";

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setContentType("text/html; charset=" + UTF8);
        request.setCharacterEncoding(UTF8);
        response.setCharacterEncoding(UTF8);
        filterChain.doFilter(request, response);
    }
}
