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

import eu.delving.core.storage.UserRepo;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.core.util.web.TokenReplyEmailSender;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class UserManagementController {

    @Autowired
    private TokenReplyEmailSender tokenReplyEmailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping("/mine.html")
    public ModelAndView personalPage(HttpServletRequest request) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("mine");
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.MY_EUROPEANA);
        return page;
    }

    @RequestMapping("/remember-me-theft.html")
    public ModelAndView cookieTheftHandler(HttpServletRequest request) throws Exception {
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.LOGOUT_COOKIE_THEFT);
        return ControllerUtil.createModelAndViewPage("redirect:/login.html");
    }

    @RequestMapping("/login.html")
    public ModelAndView login(
            HttpServletRequest request,
            @RequestParam(required = false) String error
    ) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("login");
        page.addObject("errorMessage", "1".equals(error));
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.LOGIN, page);
        return page;
    }

    @RequestMapping("/forgot-password.html")
    public ModelAndView forgotPassword(
            HttpServletRequest request,
            @RequestParam(required = false) String email
    ) throws IOException, TemplateException {
        ModelAndView page = ControllerUtil.createModelAndViewPage("forgot-password");
        String state = "";
        if (email != null) {
            String registerUri = request.getRequestURL().toString();
            int lastSlash = registerUri.lastIndexOf("/");
            if (!ControllerUtil.validEmailAddress(email)) {
                state = "formatFailure";
            }
            else if (userRepo.byEmail(email) == null) {
                state = "nonexistentFailure";
            }
            else {
                registerUri = registerUri.substring(0, lastSlash + 1) + "change-password.html";
                tokenReplyEmailSender.sendForgotPasswordEmail(email, registerUri);
                state = "success";
            }
        }
        page.addObject("email", email);
        page.addObject("state", state);
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.FORGOT_PASSWORD, page);
        return page;
    }

    @RequestMapping("/register-request.html")
    public ModelAndView registerRequest(
            HttpServletRequest request,
            @RequestParam(required = false) String email
    ) throws IOException, TemplateException {
        ModelAndView page = ControllerUtil.createModelAndViewPage("register-request");
        String state = "";
        if (email != null) {
            String registerUri = request.getRequestURL().toString();
            int lastSlash = registerUri.lastIndexOf("/");
            if (!ControllerUtil.validEmailAddress(email)) {
                state = "formatFailure";
            }
            else if (userRepo.byEmail(email) != null) {
                state = "existenceFailure";
            }
            else {
                registerUri = registerUri.substring(0, lastSlash + 1) + "register.html";
                tokenReplyEmailSender.sendRegisterEmail(email, registerUri);
                state = "success";
            }
        }
        page.addObject("email", email);
        page.addObject("state", state);
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER_REQUEST, page);
        return page;
    }

    @RequestMapping("/logout.html")
    public ModelAndView logout(HttpServletRequest request) throws Exception {
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.LOGOUT);
        return ControllerUtil.createModelAndViewPage("logout");
    }

    @RequestMapping("/logout-success.html")
    public ModelAndView logoutSuccess(HttpServletRequest request) throws Exception {
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.LOGOUT);
        return ControllerUtil.createModelAndViewPage("redirect:/index.html");
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
        clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.REDIRECT_TO_SECURE, "redirect=" + redirect);
        return ControllerUtil.createModelAndViewPage("redirect:" + redirect);
    }
}
