package eu.europeana.controller.util;

import eu.europeana.database.domain.Token;
import eu.europeana.query.EuropeanaQueryException;
import freemarker.template.TemplateException;

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
    private String from;

    private TokenService tokenService;

    public void setEmailSenders(Map<String, EmailSender> emailSenders) {
        this.emailSenders = emailSenders;
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String sendEmail(String emailAddress, String url, String subject, String action) throws IOException, TemplateException {
        Token token = tokenService.createNewToken(emailAddress);
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("url", formatUrl(url, token.getToken()));
        EmailSender sender = emailSenders.get(action);
        if (sender == null) {
            throw new IllegalArgumentException("No email sender for action ["+action+"]!");
        }
        sender.sendEmail(emailAddress, from, subject, model);
        return token.getToken();
    }

    private String formatUrl(String url, String token) {
        return url + "?token=" + token;
    }

    public String getEmailForToken(String tokenString) throws EuropeanaQueryException {
        Token token = tokenService.getToken(tokenString);
        return token.getEmail();
    }

}