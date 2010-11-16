package eu.delving.web;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Make authentication work with ajax
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AjaxAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private AuthenticationFailureHandler defaultHandler;

    public void setDefaultHandler(AuthenticationFailureHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if ("true".equals(request.getHeader("X-Ajax-call"))) {
            response.getWriter().print("fail");
            response.getWriter().flush();
        }
        else {
            defaultHandler.onAuthenticationFailure(request, response, exception);
        }
    }
}
