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

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Token;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.EmailSender;
import eu.europeana.core.util.web.TokenService;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * This Controller allows people to change their passwords
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/change-password.html")
public class ChangePasswordController {
    private Logger log = Logger.getLogger(getClass());

    @Value("#{europeanaProperties['admin.to']}")
    private String adminTo;

    @Value("#{europeanaProperties['system.from']}")
    private String systemFrom;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Qualifier("emailSenderForPasswordChangeNotify")
    private EmailSender notifyEmailSender;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChangePasswordFormValidator());
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String getMethod(@RequestParam("token") String tokenKey,
                               @ModelAttribute("command") ChangePasswordForm form,
                               HttpServletRequest request) throws Exception {
        Token token = tokenService.getToken(tokenKey);
        if (token == null) {
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.ERROR_TOKEN_EXPIRED);
            return "token-expired";
        }
        form.setToken(token.getToken());
        form.setEmail(token.getEmail());
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.CHANGE_PASSWORD_SUCCES);
        return "change-password";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String post(@Valid @ModelAttribute("command") ChangePasswordForm form,
                          BindingResult result,
                          HttpServletRequest request) throws Exception {
        if (result.hasErrors()) {
            log.info("The change password form has errors");
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.CHANGE_PASSWORD_FAILURE);
            return "change-password";
        }
        Token token = tokenService.getToken(form.getToken()); //token is validated in handleRequestInternal
        User user = userDao.fetchUserByEmail(token.getEmail()); //don't use email from the form. use token.
        if (user == null) {
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.REGISTER_FAILURE);
            throw new RuntimeException("Expected to find user for "+token.getEmail());
        }
        user.setPassword(form.getPassword());
        tokenService.removeToken(token); //remove token. it can not be used any more.
        userDao.updateUser(user); //now update the user
        sendNotificationEmail(user);
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.REGISTER_SUCCESS);
        return "change-password-success";
    }

    private void sendNotificationEmail(User user) {
        try {
            Map<String, Object> model = new TreeMap<String, Object>();
            model.put("user", user);
            notifyEmailSender.sendEmail(adminTo, systemFrom, "Password Changed", model);
        }
        catch (Exception e) {
            log.warn("Unable to send email to " + adminTo, e);
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

    public class ChangePasswordFormValidator implements Validator {

        public boolean supports(Class aClass) {
            return ChangePasswordForm.class.equals(aClass);
        }

        public void validate(Object o, Errors errors) {
            ChangePasswordForm form = (ChangePasswordForm) o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "password2.required", "Repeat Password is required");

            if (!form.getPassword().equals(form.getPassword2())) {
                errors.rejectValue("password", "password.mismatch", "Passwords do not match");
            }
        }
    }
}