package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.controller.util.EmailSender;
import eu.europeana.database.domain.User;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Eric van der Meulen <eric.meulen@gmail.com>, vitali
 */

public class ContactController extends SimpleFormController {

    private EmailSender emailSender;

    private EmailSender feedbackConfirmationEmailSender;

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void setFeedbackConfirmationEmailSender(EmailSender feedbackConfirmationEmailSender) {
        this.feedbackConfirmationEmailSender = feedbackConfirmationEmailSender;
    }

    public ContactController() {
        setValidator(new ContactValidator());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ContactForm form = new ContactForm();
        if (! isFormSubmission(request)) {
            User user = ControllerUtil.getUser();
            if (user != null) {
                form.setEmail( user.getEmail() );
            }
        }
        return form;
    }

    @Override
    protected void doSubmitAction(Object o) throws Exception {
        WebApplicationContext ctx = getWebApplicationContext();
        Map config = (Map) ctx.getBean("config");

        ContactForm form = (ContactForm) o;
        Map<String,Object> templateModel = new TreeMap<String,Object>();
        templateModel.put("email",    form.getEmail());
        templateModel.put("feedback", form.getFeedbackText());

        emailSender.sendEmail(
                (String) config.get("feedback.to"),
                (String) config.get("feedback.from"),
                "User Feedback",
                templateModel
        );

        feedbackConfirmationEmailSender.sendEmail(
                form.getEmail(), // user's email
                (String) config.get("feedback.from"),
                "User Feedback",
                templateModel
        );
        //TODO i18n subjects above

        form.setSubmitMessage( "Your feedback was successfully sent. Thank you!"); //TODO i18n
    }

    public static class ContactForm {
        String email          = ""; //empty by default (for freemarker)
        String feedbackText   = ""; //empty by default (for freemarker)
        String submitMessage  = null;

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

    public class ContactValidator implements Validator {

        public boolean supports(Class aClass) {
            return ContactForm.class.equals(aClass);
        }

        public void validate(Object o, Errors errors) {
            ContactForm form = (ContactForm)o;
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.required", "Email is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "feedbackText", "feedbackText.required", "Please enter some feedback text");

            if (!ControllerUtil.validEmailAddress(form.getEmail())) {
                errors.rejectValue("email", "email.invalidEmail", "Please enter a valid email address");
            }
        }
    }

}