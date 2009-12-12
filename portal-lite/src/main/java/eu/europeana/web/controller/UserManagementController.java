package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class UserManagementController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping("/myeuropeana.html")
    public ModelAndView myEuropeanaHandler(HttpServletRequest request) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("myeuropeana");
        User user = ControllerUtil.getUser();
        if (user != null) {
            ControllerUtil.setUser(userDao.updateUser(user));
        }
        return page;
    }

    /**
     * Where we say goodbye
     *
     * @param request
     * @return
     * @throws Exception
     * @author Gerald de Jong <geralddejong@gmail.com>
     */
    @RequestMapping("/logout.html")
    public ModelAndView logoutHandler(HttpServletRequest request) throws Exception {
        return ControllerUtil.createModelAndViewPage("logout");
    }

    /*
     * This is a controller which just redirects back to the same place without the "secure" part of the URL, which is
     * to be marked as requiring ROLE_USER in the Spring security set up.  This causes a normally insecure page to
     * trigger a security login.
     *
     * @author Gerald de Jong <geralddejong@gmail.com>
     */

    @RequestMapping("/secure/*.html")
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String SECURE = "secure/";
        String url = request.getRequestURL().toString() + "?" + request.getQueryString();
        int securePos = url.indexOf(SECURE);
        if (securePos < 0) {
            throw new IllegalArgumentException("Expected to find '" + SECURE + "' in the request URL");
        }
        String redirect = url.substring(0, securePos) + url.substring(securePos + SECURE.length());
        log.info("redirecting to: " + redirect);
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }
}