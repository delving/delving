package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.TokenReplyEmailSender;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 * Where people give us their password
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class LoginController extends AbstractPortalController {
    private Logger log = Logger.getLogger(getClass());
    private TokenReplyEmailSender tokenReplyEmailSender;
    private UserDao userDao;

    public void setRegistrationEmailSender(TokenReplyEmailSender tokenReplyEmailSender) {
        this.tokenReplyEmailSender = tokenReplyEmailSender;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("login");
        String email = request.getParameter("email");

        boolean failureFormat = false;
        boolean failureExists = false;
        boolean success = false;

        boolean failureForgotFormat = false;
        boolean failureForgotDoesntExist = false;
        boolean forgotSuccess = false;

        if (email != null) {
            log.info("email submitted: ["+email+"]");

            String registerUri = request.getRequestURL().toString();
            int lastSlash = registerUri.lastIndexOf("/");
            String buttonPressed = request.getParameter("submit_login");

            //Register
            if ("Register".equals(buttonPressed)) {  //TODO this value is internationalized in the template
                if (! ControllerUtil.validEmailAddress(email)) {
                    failureFormat = true;
                }
                else if (emailExists(email)) {
                    failureExists = true;
                }
                else {
                    registerUri = registerUri.substring(0, lastSlash + 1) + "register.html";
                    tokenReplyEmailSender.sendEmail(email, registerUri, "My Europeana email confirmation", "register");
                    success = true;
                }
            }

            //Forgot Password
            else if ("Request".equals(buttonPressed)) {
                if (! ControllerUtil.validEmailAddress(email)) {
                    failureForgotFormat = true;
                }
                else if (! emailExists(email)) {
                    failureForgotDoesntExist = true;
                }
                else {
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
        model.put("errorMessage", "1".equals(request.getParameter("error")) ? "Invalid Credentials" : null ); //TODO i18n
        model.put("register", register);
        model.put("email", email);
        model.put("success", success);
        model.put("forgotSuccess", forgotSuccess);
        model.put("failure", failureFormat || failureExists || failureForgotFormat || failureForgotDoesntExist);
        model.put("failureFormat", failureFormat);
        model.put("failureExists", failureExists);
        model.put("failureForgotFormat", failureForgotFormat);
        model.put("failureForgotDoesntExist", failureForgotDoesntExist);
    }

    private boolean emailExists(String email) {
        return userDao.fetchUserByEmail(email) != null;
    }
}