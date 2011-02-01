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
import eu.delving.core.util.SimpleMessageCodesResolver;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.EmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import javax.validation.Valid;

/**
 * This Controller allows people to change their passwords
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/change-password.html")
public class ChangePasswordController {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private EmailSender emailSender;

    @Value("#{launchProperties['system.from']}")
    private String fromEmail;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ChangePasswordFormValidator());
        binder.setMessageCodesResolver(new SimpleMessageCodesResolver());
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String getMethod(@RequestParam("token") String tokenKey,
                               @ModelAttribute("command") ChangePasswordForm command,
                               HttpServletRequest request) throws Exception {
        TokenRepo.RegistrationToken token = tokenRepo.getRegistrationToken(tokenKey);
        if (token == null) {
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.ERROR_TOKEN_EXPIRED);
            return "token-expired";
        }
        command.setToken(token.getId());
        command.setEmail(token.getEmail());
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.CHANGE_PASSWORD_SUCCES);
        return "change-password";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String post(@Valid @ModelAttribute("command") ChangePasswordForm command,
                          BindingResult result,
                          HttpServletRequest request) throws Exception {
        if (result.hasErrors()) {
            log.info("The change password form has errors");
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.CHANGE_PASSWORD_FAILURE);
            return "change-password";
        }
        TokenRepo.RegistrationToken token = tokenRepo.getRegistrationToken(command.getToken()); //token is validated in handleRequestInternal
        User user = userRepo.byEmail(token.getEmail()); //don't use email from the form. use token.
        if (user == null) {
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER_FAILURE);
            throw new RuntimeException("Expected to find user for " + token.getEmail());
        }
        user.setPassword(command.getPassword());
        user.save();
        token.delete();
        emailSender.
                create("password-change-notify").
                setFrom(fromEmail).
                setTo(user.getEmail()).
                set("user", user).
                send();
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.REGISTER_SUCCESS);
        return "change-password-success";
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

        @Override
        public boolean supports(Class aClass) {
            return ChangePasswordForm.class.equals(aClass);
        }

        @Override
        public void validate(Object o, Errors errors) {
            ChangePasswordForm form = (ChangePasswordForm) o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "_mine.user.register.requiredfield", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password2", "_mine.user.register.requiredfield", "Repeat Password is required");

            if (!form.getPassword().equals(form.getPassword2())) {
                errors.rejectValue("password", "_mine.user.validation.password.mismatch", "Passwords do not match");
            }
        }
    }
}
