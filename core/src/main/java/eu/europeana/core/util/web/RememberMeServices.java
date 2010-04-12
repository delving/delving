package eu.europeana.core.util.web;

import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A subclass of the services class which sets the cookie path to root
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RememberMeServices extends PersistentTokenBasedRememberMeServices {
    public RememberMeServices() throws Exception {
    }

    @Override
    public void setCookie(String [] tokens, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
