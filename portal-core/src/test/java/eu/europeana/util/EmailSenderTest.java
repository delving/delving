package eu.europeana.util;

import org.junit.Test;
import org.junit.Assert;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import freemarker.template.Template;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class EmailSenderTest {
    private static final String TEMPLATE_TXT = "TEST TEMPLATE _${user}_";
    private static final String TEMPLATE_HTML = "TEST TEMPLATE <b>${user}</b>";
    private static final String USER_NAME = "XXX";


    /**
     * @author Borys Omelayenko
     */

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
                } catch (Exception e) {
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
        Map<String,Object> model = new HashMap<String, Object>();
        model.put("user", USER_NAME);
        sender.setMailSender(new TestMailSender());
        sender.setTemplate("tmpl");
        sender.sendEmail(
                "dummy@dummy.never-be-a-tld",
                "dummy@dummy.never-be-a-tld",
                "test email, you should never see it",
                model);

    }

}
