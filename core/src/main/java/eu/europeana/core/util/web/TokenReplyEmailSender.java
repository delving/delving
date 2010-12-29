package eu.europeana.core.util.web;

import eu.delving.core.storage.Token;
import eu.delving.core.storage.TokenService;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handle registration email sending
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TokenReplyEmailSender {
    private Map<String,EmailSender> emailSenders;

    @Autowired
    private TokenService tokenService;

    public void setEmailSenders(Map<String, EmailSender> emailSenders) {
        this.emailSenders = emailSenders;
    }

    public String sendEmail(String emailAddress, String url, String action) throws IOException, TemplateException {
        Token token = tokenService.createNewToken(emailAddress);
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("url", url + "?token=" + token.getToken());
        model.put(EmailSender.TO_EMAIL, emailAddress);
        EmailSender sender = emailSenders.get(action);
        if (sender == null) {
            throw new IllegalArgumentException("No email sender for action ["+action+"]!");
        }
        sender.sendEmail(model);
        return token.getToken();
    }
}