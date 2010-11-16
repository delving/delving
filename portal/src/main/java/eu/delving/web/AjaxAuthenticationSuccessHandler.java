package eu.delving.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Make authentication work with ajax
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private AuthenticationSuccessHandler defaultHandler;

    public void setDefaultHandler(AuthenticationSuccessHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if ("true".equals(request.getHeader("X-Ajax-call"))) {
            response.getWriter().print("ok");
            response.getWriter().flush();
        }
        else {
            defaultHandler.onAuthenticationSuccess(request, response, authentication);
        }

    }
}
