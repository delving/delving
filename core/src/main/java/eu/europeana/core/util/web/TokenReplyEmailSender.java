package eu.europeana.core.util.web;

import eu.delving.core.storage.TokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Handle registration email sending
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TokenReplyEmailSender {

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private EmailSender emailSender;

    @Value("#{launchProperties['system.from']}")
    private String fromEmail;

    public String sendRegisterEmail(String emailAddress, String baseUrl) {
        TokenRepo.RegistrationToken token = tokenRepo.createRegistrationToken(emailAddress);
        emailSender.
                create("confirmation").
                setFrom(fromEmail).
                setTo(emailAddress).
                set("url", baseUrl + "?token=" + token.getId()).
                send();
        return token.getId();
    }

    public String sendForgotPasswordEmail(String emailAddress, String baseUrl) {
        TokenRepo.RegistrationToken token = tokenRepo.createRegistrationToken(emailAddress);
        emailSender.
                create("forgot-password").
                setFrom(fromEmail).
                setTo(emailAddress).
                set("url", baseUrl + "?token=" + token.getId()).
                send();
        return token.getId();
    }

}