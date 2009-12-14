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

package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView myEuropeanaHandler() throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("myeuropeana");
        User user = ControllerUtil.getUser();
        if (user != null) {
            ControllerUtil.setUser(userDao.updateUser(user));
        }
        return page;
    }

    @RequestMapping("/logout.html")
    public ModelAndView logoutHandler() throws Exception {
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
        log.info("redirecting to: " + redirect);
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }
}