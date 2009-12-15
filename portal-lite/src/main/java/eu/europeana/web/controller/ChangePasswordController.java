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

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Token;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.EmailSender;
import eu.europeana.web.util.TokenService;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

import java.util.Map;
import java.util.TreeMap;

/**
 * This Controller allows people to change their passwords
 *
 * @author vitali
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/change-password.html")
public class ChangePasswordController implements ApplicationContextAware {
    private Logger log = Logger.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Qualifier("emailSenderForPasswordChangeNotify")
    private EmailSender notifyEmailSender;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChangePasswordFormValidator());
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String getMethod(@RequestParam("token") String tokenKey, @ModelAttribute("command") ChangePasswordForm form) throws Exception {
        Token token = tokenService.getToken(tokenKey);
        if (token == null) {
            return "token-expired";
        }
        form.setToken(token.getToken());
        form.setEmail(token.getEmail());
        return "change-password";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String post(@Valid @ModelAttribute("command") ChangePasswordForm form, BindingResult result) throws Exception {
        if (result.hasErrors()) {
            log.info("The change password form has errors");
            return "change-password";
        }
        Token token = tokenService.getToken(form.getToken()); //token is validated in handleRequestInternal
        User user = userDao.fetchUserByEmail(token.getEmail()); //don't use email from the form. use token.
        if (user == null) {
            throw new RuntimeException("Expected to find user for "+token.getEmail());
        }
        user.setPassword(form.getPassword());
        tokenService.removeToken(token); //remove token. it can not be used any more.
        userDao.updateUser(user); //now update the user

        //send email notification
        // todo: this is awkward.. a better solution should be found.  Inject these things?
        Map config = (Map) applicationContext.getBean("config");
        Map<String, Object> model = new TreeMap<String, Object>();
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
        return "register-success"; // todo: strange to go here, isn't it?
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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