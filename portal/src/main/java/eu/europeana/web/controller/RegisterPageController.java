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

package eu.europeana.web.controller;

import eu.delving.core.storage.TokenRepo;
import eu.delving.core.storage.User;
import eu.delving.core.storage.UserRepo;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.EmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TokenRepo tokenRepo;

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
    protected String getRequest(@RequestParam("token") String tokenKey, @ModelAttribute("command") RegistrationForm regForm, HttpServletRequest request) throws EuropeanaQueryException { // todo: query exception??
        log.info("Received get request, putting token into registration form model attribute");
        TokenRepo.RegistrationToken token = tokenRepo.getRegistrationToken(tokenKey);
        if (token == null) {
            throw new EuropeanaQueryException("Registration must be retried, no token for "+tokenKey);  // todo: query exception??
        }
        regForm.setToken(token.getId());
        regForm.setEmail(token.getEmail());
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER);
        return "register";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String formSubmit(@ModelAttribute("command") RegistrationForm form, BindingResult result, HttpServletRequest request) throws EuropeanaQueryException { // todo: query exception??
        if (result.hasErrors()) {
            log.info("The registration form has errors");
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER_FAILURE);
            return "register";
        }
        TokenRepo.RegistrationToken token = tokenRepo.getRegistrationToken(form.getToken()); //the token was validated in handleRequestInternal
        token.delete();
        User user = userRepo.createUser(token.getEmail());//use email from token. not from form.
        user.setUserName(form.getUserName());
        user.setPassword(form.getPassword());
        user.setEnabled(true);
        user.setRole(User.Role.ROLE_USER);
        user.save();
        sendNotificationEmail(user);
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER_SUCCESS);
        return "register-success";
    }

    private void sendNotificationEmail(User user) {
        try {
            Map<String, Object> model = new TreeMap<String, Object>();
            model.put("user", user);
            notifyEmailSender.sendEmail(model);
        }
        catch (Exception e) {
            log.warn("Unable to send email to " + notifyEmailSender.getToEmail(), e);
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
            if (userName == null && email != null) {
                userName = email.substring(0, email.indexOf("@")).toLowerCase();
            }
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

        @Override
        public boolean supports(Class aClass) {
            return RegistrationForm.class.equals(aClass);
        }

        @Override
        public void validate(Object o, Errors errors) {
            RegistrationForm form = (RegistrationForm) o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "username.required", "Username is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "password2.required", "Repeat Password is required");

            if (!userRepo.isProperUserName(form.getUserName())) {
                errors.rejectValue("userName", "_mine.user.validation.username.invalid", "User name invalid");
            }

            if (!form.getPassword().equals(form.getPassword2())) {
                errors.rejectValue("password", "_mine.user.validation.password.mismatch", "Passwords do not match");
            }

            // password less then 6 characters
            if (form.getPassword().length() < 6) {
                errors.rejectValue("password", "_mine.user.validation.password.tooshort", "Password is too short");
            }

            if (form.getPassword().length() > 30) {
                errors.rejectValue("password", "_mine.user.validation.password.long", "Password is too long");
            }
            if (!form.getDisclaimer()) {
                errors.rejectValue("disclaimer", "_mine.user.validation.disclaimer.unchecked", "Disclaimer must be accepted");
            }
        }
    }
}
