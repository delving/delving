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

import eu.europeana.core.database.domain.User;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.core.util.web.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * This controller handles the contact information
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/contact.html")
public class ContactPageController {

    @Autowired
    @Qualifier("emailSenderForUserFeedback")
    private EmailSender userFeedbackSender;

    @Autowired
    @Qualifier("emailSenderForUserFeedbackConfirmation")
    private EmailSender userFeedbackConfirmSender;

    @Value("#{europeanaProperties['feedback.to']}")
    private String feedbackTo;

    @Value("#{europeanaProperties['feedback.from']}")
    private String feedbackFrom;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new ContactFormValidator());
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
    protected String handlePost(@ModelAttribute("command") @Valid ContactForm form, BindingResult result, HttpServletRequest request) throws Exception {
        if (result.hasErrors()) {
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.FEEDBACK_SEND_FAILURE);
        }
        else {
            Map<String, Object> model = new TreeMap<String, Object>();
            model.put("email", form.getEmail());
            model.put("feedback", form.getFeedbackText());
            userFeedbackSender.sendEmail(feedbackTo, feedbackFrom, "User Feedback", model);
            userFeedbackConfirmSender.sendEmail(form.getEmail(), feedbackFrom, "User Feedback", model);
            form.setSubmitMessage("Your feedback was successfully sent. Thank you!");
            clickStreamLogger.log(request, ClickStreamLogger.UserAction.FEEDBACK_SEND);
        }
        clickStreamLogger.log(request, ClickStreamLogger.UserAction.CONTACT_PAGE);
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

        public boolean supports(Class aClass) {
            return ContactForm.class.equals(aClass);
        }

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