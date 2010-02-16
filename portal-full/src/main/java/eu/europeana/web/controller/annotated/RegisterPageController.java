/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
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
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.Token;
import eu.europeana.database.domain.User;
import eu.europeana.query.ClickStreamLogger;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryProblem;
import eu.europeana.web.util.EmailSender;
import eu.europeana.web.util.TokenService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * During registration, users click on an email link to end up here with a "token" that allows them to
 * proceed with registration.  They have to choose a user name, password etc.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/register.html")
public class RegisterPageController {
    private Logger log = Logger.getLogger(getClass());

    @Value("#{europeanaProperties['register.to']}")
    private String registerTo;

    @Value("#{europeanaProperties['system.from']}")
    private String systemFrom;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Qualifier("emailSenderForRegisterNotify")
    private EmailSender notifyEmailSender;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new RegistrationFormValidator());
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String getRequest(
            @RequestParam("token") String tokenKey,
            @ModelAttribute("command") RegistrationForm regForm,
            HttpServletRequest request) throws EuropeanaQueryException {
        log.info("Received get request, putting token into registration form model attribute");
        Token token = tokenService.getToken(tokenKey);
        if (token == null) {  //when token is null, show useful message
            throw new EuropeanaQueryException(QueryProblem.UNKNOWN_TOKEN.toString());
        }
        regForm.setToken(token.getToken());
        regForm.setEmail(token.getEmail());
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.REGISTER);
        return "register";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String formSubmit(@Valid @ModelAttribute("command") RegistrationForm regForm,
                                BindingResult result,
                                HttpServletRequest request)
            throws EuropeanaQueryException {
        if (result.hasErrors()) {
            log.info("The registration form has errors");
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.REGISTER_FAILURE);
            return "register";
        }
        Token token = tokenService.getToken(regForm.getToken()); //the token was validated in handleRequestInternal
        User user = new User();
        user.setEmail(token.getEmail());  //use email from token. not from form.
        user.setUserName(regForm.getUserName());
        user.setPassword(regForm.getPassword());
        user.setRegistrationDate(new Date());
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        tokenService.removeToken(token);    //remove token. it can not be used any more.
        userDao.addUser(user);  //finally save the user.
        sendNotificationEmail(user);
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.REGISTER_SUCCESS);
        return "register-success";
    }

    private void sendNotificationEmail(User user) {
        try {
            Map<String, Object> model = new TreeMap<String, Object>();
            model.put("user", user);
            notifyEmailSender.sendEmail(registerTo, systemFrom, "Somebody Registerd", model);
        }
        catch (Exception e) {
            log.warn("Unable to send email to " + registerTo, e);
        }
    }

    public static class RegistrationForm {
        private String token;
        private String email;
        private String userName;
        private String password;
        private String password2;
        private Boolean disclaimer;

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

    public class RegistrationFormValidator implements Validator {

        public boolean supports(Class aClass) {
            return RegistrationForm.class.equals(aClass);
        }

        public void validate(Object o, Errors errors) {
            RegistrationForm form = (RegistrationForm) o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "username.required", "Username is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "password2.required", "Repeat Password is required");

            if (!validUserName(form.getUserName())) {
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
                if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '_')) {
                    return false;
                }
            }
            return true;
        }
    }
}