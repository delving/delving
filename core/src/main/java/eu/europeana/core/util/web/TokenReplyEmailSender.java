/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.util.web;

import eu.delving.core.storage.TokenRepo;
import eu.delving.core.util.EmailTarget;
import eu.delving.core.util.ThemeFilter;
import org.springframework.beans.factory.annotation.Autowired;

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

    public String sendRegisterEmail(String emailAddress, String baseUrl) {
        final EmailTarget emailTarget = ThemeFilter.getTheme().getEmailTarget();
        TokenRepo.RegistrationToken token = tokenRepo.createRegistrationToken(emailAddress);
        emailSender.
                create("confirmation").
                setFrom(emailTarget.getSystemFrom()).
                setTo(emailAddress).
                set("url", baseUrl + "?token=" + token.getId()).
                send();
        return token.getId();
    }

    public String sendForgotPasswordEmail(String emailAddress, String baseUrl) {
        final EmailTarget emailTarget = ThemeFilter.getTheme().getEmailTarget();
        TokenRepo.RegistrationToken token = tokenRepo.createRegistrationToken(emailAddress);
        emailSender.
                create("forgot-password").
                setFrom(emailTarget.getSystemFrom()).
                setTo(emailAddress).
                set("url", baseUrl + "?token=" + token.getId()).
                send();
        return token.getId();
    }

}