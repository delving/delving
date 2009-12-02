package eu.europeana.controller;

import eu.europeana.controller.util.EmailSender;
import eu.europeana.controller.util.TokenService;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Token;
import eu.europeana.database.domain.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author vitali
 */
public class ChangePasswordController extends SimpleFormController {
    private Logger log = Logger.getLogger(getClass());

    private UserDao userDao;
    private TokenService tokenService;
    private EmailSender notifyEmailSender;

    public ChangePasswordController() {
        setValidator(new UserValidator());
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    public void setNotifyEmailSender(EmailSender emailSender) {
        this.notifyEmailSender = emailSender;
    }
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String token = request.getParameter("token");
        Token rToken = tokenService.getToken(token);
        if (rToken == null) {
            return new ModelAndView("token-expired");
        }
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ChangePasswordForm form = new ChangePasswordForm();
        if (! isFormSubmission(request)) {
            String token = request.getParameter("token");
            Token rToken = tokenService.getToken(token);
            form.setToken( rToken.getToken() );
            form.setEmail( rToken.getEmail() );
        }
        return form;
    }

    @Override
    protected void doSubmitAction(Object o) throws Exception {
        ChangePasswordForm form = (ChangePasswordForm) o;
        Token token = tokenService.getToken( form.getToken() ); //token is validated in handleRequestInternal
        User user = userDao.fetchUserByEmail( token.getEmail() ); //don't use email from the form. use token.
        user.setPassword        ( form.getPassword() );

        tokenService.removeToken(token); //remove token. it can not be used any more.

        userDao.updateUser( user ); //now update the user

        //send email notification
        WebApplicationContext ctx = getWebApplicationContext();
        Map config = (Map) ctx.getBean("config");

        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("user", user);
        try {
            notifyEmailSender.sendEmail(
                    (String) config.get("admin.to"),
                    (String) config.get("system.from"),
                    "Password Changed",
                    model); //TODO subject
        }
        catch (Exception e) {
            log.warn("Unable to send email to " + config.get("admin.to"), e);
        }
    }

    public static class ChangePasswordForm {
        String token;
        String email;
        String password;
        String password2;

        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getPassword2() {
            return password2;
        }
        public void setPassword2(String password2) {
            this.password2 = password2;
        }
    }

    public class UserValidator implements Validator {

        public boolean supports(Class aClass) {
            return ChangePasswordForm.class.equals(aClass);
        }

        public void validate(Object o, Errors errors) {
            ChangePasswordForm form = (ChangePasswordForm)o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "password2.required", "Repeat Password is required");

            if (!form.getPassword().equals(form.getPassword2())) {
                errors.rejectValue("password", "password.mismatch", "Passwords do not match");
            }
        }
    }
}