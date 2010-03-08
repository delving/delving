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

package eu.europeana.web.util;

import eu.europeana.core.util.web.EmailSender;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmailSenderTest {
    private static final String TEMPLATE_TXT = "TEST TEMPLATE _${user}_";
    private static final String TEMPLATE_HTML = "TEST TEMPLATE <b>${user}</b>";
    private static final String USER_NAME = "XXX";


    @Test
    public void testBasic() throws Exception {

        /*
        Inspects the message.
         */
        class TestMailSender extends JavaMailSenderImpl {
            @Override
            public void send(MimeMessage[] mimeMessages) throws MailException {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    mimeMessages[0].writeTo(os);
                    String messageText = os.toString();
                    /*
                    Here we check the email message created by EmailSender
                     */
                    Assert.assertTrue(messageText.contains("_" + USER_NAME + "_"));
                    Assert.assertTrue(messageText.contains("<b>" + USER_NAME + "</b>"));
                }
                catch (Exception e) {
                    throw new MailPreparationException(e);
                }
            }
        }

        /*
        Provides a pre-defined template.
         */
        EmailSender sender = new EmailSender() {
            @Override
            protected Template getResourceTemplate(String fileName) throws IOException {
                Configuration configuration = new Configuration();
                configuration.setLocale(new Locale("nl"));
                configuration.setObjectWrapper(new DefaultObjectWrapper());
                return new Template(
                        "testTemplate",
                        new StringReader(fileName.contains("html") ? TEMPLATE_HTML : TEMPLATE_TXT),
                        configuration);
            }
        };

        /*
        Test
         */
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("user", USER_NAME);
        sender.setMailSender(new TestMailSender());
        sender.setTemplate("tmpl");
        sender.setToEmail("dummy@dummy.never-be-a-tld");
        sender.setFromEmail("dummy@dummy.never-be-a-tld");
        sender.setSubject("test email, you should never see it");
        sender.sendEmail(model);
    }

}
