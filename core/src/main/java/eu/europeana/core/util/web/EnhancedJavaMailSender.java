package eu.europeana.core.util.web;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * handle gmail variation
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EnhancedJavaMailSender extends JavaMailSenderImpl {

    @Override
    public void setUsername(String userName) {
        if (userName.contains("gmail.com")) {
            Properties props = new Properties();
            props.put("mail.smtp.host","smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            super.setJavaMailProperties(props);
        }
        super.setUsername(userName);
    }
}