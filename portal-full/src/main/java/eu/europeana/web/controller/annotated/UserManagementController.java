/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.controller.annotated;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.User;
import eu.europeana.query.ClickStreamLogger;
import eu.europeana.web.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class UserManagementController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping("/myeuropeana.html")
    public ModelAndView myEuropeanaHandler(HttpServletRequest request) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("myeuropeana");
        User user = ControllerUtil.getUser();
        if (user != null) {
            ControllerUtil.setUser(userDao.updateUser(user));
        }
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.MY_EUROPEANA);
        return page;
    }

    @RequestMapping("/logout-success.html")
    public ModelAndView logoutSuccessHandler(HttpServletRequest request) throws Exception {
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.LOGOUT);
        return ControllerUtil.createModelAndViewPage("redirect:/index.html");
    }

    @RequestMapping("/remember-me-theft.html")
    public ModelAndView cookieTheftHandler(HttpServletRequest request) throws Exception {
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.LOGOUT_COOKIE_THEFT);
        return ControllerUtil.createModelAndViewPage("redirect:/login.html");
    }

    @RequestMapping("/logout.html")
    public ModelAndView logoutHandler(HttpServletRequest request) throws Exception {
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.LOGOUT);
        return ControllerUtil.createModelAndViewPage("logout");
    }

    /*
     * This is a controller which just redirects back to the same place without the "secure" part of the URL, which is
     * to be marked as requiring ROLE_USER in the Spring security set up.  This causes a normally insecure page to
     * trigger a security login.
     */

    @RequestMapping("/secure/*.html")
    protected ModelAndView secureHtml(HttpServletRequest request) throws Exception {
        String SECURE = "secure/";
        String url = request.getRequestURL().toString() + "?" + request.getQueryString();
        int securePos = url.indexOf(SECURE);
        if (securePos < 0) {
            throw new IllegalArgumentException("Expected to find '" + SECURE + "' in the request URL");
        }
        String redirect = url.substring(0, securePos) + url.substring(securePos + SECURE.length());
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.REDIRECT_TO_SECURE, "redirect="+redirect);
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }
}