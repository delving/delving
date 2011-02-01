/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
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

import eu.delving.core.storage.User;
import eu.delving.core.util.SimpleMessageCodesResolver;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.core.util.web.EmailSender;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * This controller handles the contact information
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/contact.html")
public class ContactPageController {

    @Value("#{launchProperties['feedback.from']}")
    private String fromEmail;

    @Value("#{launchProperties['feedback.to']}")
    private String toEmail;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ContactFormValidator());
        binder.setMessageCodesResolver(new SimpleMessageCodesResolver());
    }

    @ModelAttribute("command")
    public ContactForm createContactForm() {
        ContactForm form = new ContactForm();
        User user = ControllerUtil.getUser();
        if (user != null) {
            form.setEmail(user.getEmail());
        }
        return form;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String handleGet() {
        return "contact";
    }

    @RequestMapping(method = RequestMethod.POST)
    protected String handlePost(@Valid @ModelAttribute ContactForm command, BindingResult result, HttpServletRequest request) throws Exception {
        if (result.hasErrors()) {
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.FEEDBACK_SEND_FAILURE);
        }
        else {
            emailSender.
                    create("feedback").
                    setFrom(fromEmail).
                    setTo(toEmail).
                    set("email", command.getEmail()).
                    set("feedback", command.getFeedbackText()).
                    send();
            emailSender.
                    create("feedback-confirmation").
                    setFrom(fromEmail).
                    setTo(command.getEmail()).
                    send();
            command.setSubmitMessage("Your feedback was successfully sent. Thank you!");
            clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.FEEDBACK_SEND);
        }
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.CONTACT_PAGE);
        return "contact";
    }

    public static class ContactForm {
//        @NotEmpty(message = "Email is required")
//        @Pattern(
//                regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)",
//                message = "Please enter a valid email address"
//        )
        String email = ""; //empty by default (for freemarker)

//        @NotEmpty(message = "Please enter some feedback text")
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

    public class ContactFormValidator implements Validator {

        @Override
        public boolean supports(Class aClass) {
            return ContactForm.class.equals(aClass);
        }

        @Override
        public void validate(Object o, Errors errors) {
            ContactForm form = (ContactForm) o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.required", "Email is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "feedbackText", "feedbackText.required", "Please enter some feedback text");

            if (!ControllerUtil.validEmailAddress(form.getEmail())) {
                errors.rejectValue("email", "email.invalidEmail", "Please enter a valid email address");
            }
        }
    }

}
