package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.TokenReplyEmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Where people give us their password
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class LoginController {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private TokenReplyEmailSender tokenReplyEmailSender;
    
    @Autowired
    private UserDao userDao;

    @RequestMapping("/login.html")
    public ModelAndView handle(HttpServletRequest request) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("login");
        String email = request.getParameter("email");

        boolean failureFormat = false;
        boolean failureExists = false;
        boolean success = false;

        boolean failureForgotFormat = false;
        boolean failureForgotDoesntExist = false;
        boolean forgotSuccess = false;

        if (email != null) {
            log.info("email submitted: [" + email + "]");

            String registerUri = request.getRequestURL().toString();
            int lastSlash = registerUri.lastIndexOf("/");
            String buttonPressed = request.getParameter("submit_login");

            //Register
            if ("Register".equals(buttonPressed)) {  //TODO this value is internationalized in the template
                if (!ControllerUtil.validEmailAddress(email)) {
                    failureFormat = true;
                } else if (emailExists(email)) {
                    failureExists = true;
                } else {
                    registerUri = registerUri.substring(0, lastSlash + 1) + "register.html";
                    tokenReplyEmailSender.sendEmail(email, registerUri, "My Europeana email confirmation", "register");
                    success = true;
                }
            }

            //Forgot Password
            else if ("Request".equals(buttonPressed)) {
                if (!ControllerUtil.validEmailAddress(email)) {
                    failureForgotFormat = true;
                } else if (!emailExists(email)) {
                    failureForgotDoesntExist = true;
                } else {
                    registerUri = registerUri.substring(0, lastSlash + 1) + "change-password.html";
                    tokenReplyEmailSender.sendEmail(email, registerUri, "My Europeana reset password", "forgotPassword");
                    forgotSuccess = true;
                }
            }

            //Unknown button
            else {
                throw new IllegalArgumentException("Expected a button press to give submit_login=[Register|Request]");
            }
        }

        boolean register = true;
        page.addObject("errorMessage", "1".equals(request.getParameter("error")) ? "Invalid Credentials" : null); //TODO i18n
        page.addObject("register", register);
        page.addObject("email", email);
        page.addObject("success", success);
        page.addObject("forgotSuccess", forgotSuccess);
        page.addObject("failure", failureFormat || failureExists || failureForgotFormat || failureForgotDoesntExist);
        page.addObject("failureFormat", failureFormat);
        page.addObject("failureExists", failureExists);
        page.addObject("failureForgotFormat", failureForgotFormat);
        page.addObject("failureForgotDoesntExist", failureForgotDoesntExist);
        return page;
    }

    private boolean emailExists(String email) {
        return userDao.fetchUserByEmail(email) != null;
    }

    public void setRegistrationEmailSender(TokenReplyEmailSender tokenReplyEmailSender) {
        this.tokenReplyEmailSender = tokenReplyEmailSender;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}