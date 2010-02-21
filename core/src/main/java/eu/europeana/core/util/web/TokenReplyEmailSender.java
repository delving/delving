package eu.europeana.core.util.web;

import eu.europeana.core.database.domain.Token;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handle registration email sending
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TokenReplyEmailSender {
    public static final String TOKEN_PARAMETER="token";
    private Map<String,EmailSender> emailSenders;

    @Value("#{europeanaProperties['system.from']}")
    private String from;

    @Autowired
    private TokenService tokenService;

    public void setEmailSenders(Map<String, EmailSender> emailSenders) {
        this.emailSenders = emailSenders;
    }

    public String sendEmail(String emailAddress, String url, String subject, String action) throws IOException, TemplateException {
        Token token = tokenService.createNewToken(emailAddress);
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("url", url + "?token=" + token.getToken());
        EmailSender sender = emailSenders.get(action);
        if (sender == null) {
            throw new IllegalArgumentException("No email sender for action ["+action+"]!");
        }
        sender.sendEmail(emailAddress, from, subject, model);
        return token.getToken();
    }
}