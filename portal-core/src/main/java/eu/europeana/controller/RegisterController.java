package eu.europeana.controller;

import eu.europeana.controller.util.EmailSender;
import eu.europeana.controller.util.TokenService;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.Token;
import eu.europeana.database.domain.User;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryProblem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Where the confirmation email's link is processed
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class    RegisterController extends SimpleFormController {
    private Logger log = Logger.getLogger(getClass());

    private UserDao userDao;
    private TokenService tokenService;
    private EmailSender notifyEmailSender;

    public RegisterController() {
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
            throw new EuropeanaQueryException(QueryProblem.TOKEN_EXPIRED.toString());
        }
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        RegistrationForm form = new RegistrationForm();
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
        RegistrationForm form = (RegistrationForm) o;
        Token rToken = tokenService.getToken(form.getToken()); //the token was validated in handleRequestInternal
        User user = new User();
        user.setEmail           ( rToken.getEmail() );  //use email from token. not from form.
        user.setUserName        ( form.getUserName() );
        user.setPassword        ( form.getPassword() );
        user.setRegistrationDate( new Date() );
        user.setEnabled         ( true );
        user.setRole            ( Role.ROLE_USER );

        tokenService.removeToken(rToken);    //remove token. it can not be used any more.

        userDao.addUser( user );  //finally save the user.

        //send email notification
        WebApplicationContext ctx = getWebApplicationContext();
        Map config = (Map) ctx.getBean("config");

        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("user", user);
        try {
            notifyEmailSender.sendEmail(
                    (String) config.get("register.to"),
                    (String) config.get("system.from"),
                    "Somebody Registerd",
                    model);
        }
        catch (Exception e) {
            log.warn("Unable to send email to " + config.get("register.to"), e);
        }
    }

    public static class RegistrationForm {
        String token;
        String email;
        String userName;
        String password;
        String password2;
        Boolean disclaimer;

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
        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName = userName;
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

        public Boolean getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(Boolean disclaimer) {
            this.disclaimer = disclaimer;
        }
    }

    public class UserValidator implements Validator {

        public boolean supports(Class aClass) {
            return RegistrationForm.class.equals(aClass);
        }

        public void validate(Object o, Errors errors) {
            RegistrationForm form = (RegistrationForm)o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "username.required", "Username is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "password2.required", "Repeat Password is required");

            if ( ! validUserName(form.getUserName()) ) {
                errors.rejectValue("userName", "username.invalidChars", "Username may only contain letters, digits, spaces and underscores");
            }
            if (form.getUserName().length() > User.USER_NAME_LENGTH) {
                errors.rejectValue("userName", "username.tooLong", "Username is too long"); //TODO standard error code?
            }

            if (userDao.userNameExists(form.getUserName())) {
                errors.rejectValue("userName", "username.exists", "Username already exists");
            }

            if (!form.getPassword().equals(form.getPassword2())) {
                errors.rejectValue("password", "password.mismatch", "Passwords do not match");
            }

            // password less then 6 characters
            if (form.getPassword().length() < 6) {
                errors.rejectValue("password", "password.length", "Password is too short");
            }

            if (form.getPassword().length() > 30) {
                errors.rejectValue("password", "password.length", "Password is too long");
            }
            if (!form.getDisclaimer()) {
                errors.rejectValue("disclaimer", "disclaimer.unchecked", "Disclaimer must be accepted");
            }
        }

        private boolean validUserName(String userName) {
            //may only contain alphanumeric, spaces and underscore.
            for (int i = 0; i < userName.length(); i++) {
                char c = userName.charAt(i);
                if ( ! (Character.isLetterOrDigit(c) || c == ' ' || c == '_')) {
                    return false;
                }
            }
            return true;
        }
    }
}