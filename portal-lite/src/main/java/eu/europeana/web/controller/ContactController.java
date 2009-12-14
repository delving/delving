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

import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.EmailSender;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Eric van der Meulen <eric.meulen@gmail.com>, vitali
 */

//@Controller
public class ContactController {

    @Autowired
    @Qualifier("emailSenderForUserFeedback")
    private EmailSender emailSender;

    @Autowired
    @Qualifier("emailSenderForUserFeedbackConfirmation")
    private EmailSender feedbackConfirmationEmailSender;

    @Autowired
    @Qualifier("config")
    private Map<String, String> config;

    @ModelAttribute("contactForm")
    protected ContactForm formBackingObject(HttpServletRequest request) throws Exception {
        ContactForm form = new ContactForm();
        if (!isFormSubmission(request)) {
            User user = ControllerUtil.getUser();
            if (user != null) {
                form.setEmail(user.getEmail());
            }
        }
        return form;
    }

    private boolean isFormSubmission(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    @RequestMapping(value = "/contact.html", method=RequestMethod.GET)
    public ModelAndView handleGet(HttpServletRequest request) throws Exception {
        return ControllerUtil.createModelAndViewPage("contact");
    }

    @RequestMapping(value = "/contact.html", method=RequestMethod.POST)
    protected ModelAndView submit(@ModelAttribute("contactForm") @Valid ContactForm form) throws Exception {
        Map<String, Object> templateModel = new TreeMap<String, Object>();
        templateModel.put("email", form.getEmail());
        templateModel.put("feedback", form.getFeedbackText());

//        emailSender.sendEmail(
//                (String) config.get("feedback.to"),
//                (String) config.get("feedback.from"),
//                "User Feedback",
//                templateModel
//        );
//
//        feedbackConfirmationEmailSender.sendEmail(
//                form.getEmail(), // user's email
//                (String) config.get("feedback.from"),
//                "User Feedback",
//                templateModel
//        );
        //TODO i18n subjects above

        form.setSubmitMessage("Your feedback was successfully sent. Thank you!"); //TODO i18n

        return new ModelAndView(new RedirectView("/contact.html"));
    }

    public static class ContactForm {
        @NotEmpty(message = "Email is required")
        @Pattern(
                regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)",
                message = "Please enter a valid email address")
        String email = ""; //empty by default (for freemarker)

        @NotEmpty(message = "Please enter some feedback text")
        String feedbackText = ""; //empty by default (for freemarker)
        String submitMessage = null;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFeedbackText() {
            return feedbackText;
        }

        public void setFeedbackText(String feedbackText) {
            this.feedbackText = feedbackText;
        }

        public String getSubmitMessage() {
            return submitMessage;
        }

        public void setSubmitMessage(String submitMessage) {
            this.submitMessage = submitMessage;
        }
    }

//    public class ContactValidator implements Validator {
//
//        public boolean supports(Class aClass) {
//            return ContactForm.class.equals(aClass);
//        }
//
//        public void validate(Object o, Errors errors) {
//            ContactForm form = (ContactForm) o;
//            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.required", "Email is required");
//            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "feedbackText", "feedbackText.required", "Please enter some feedback text");
//
//            if (!ControllerUtil.validEmailAddress(form.getEmail())) {
//                errors.rejectValue("email", "email.invalidEmail", "Please enter a valid email address");
//            }
//        }
//    }

}