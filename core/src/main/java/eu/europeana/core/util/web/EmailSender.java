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

import eu.delving.core.util.ThemeInterceptor;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handle all email sending
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Borys Omelayenko <borys.omelayenko@kb.nl>
 */

public class EmailSender {
    private static final String FROM_EMAIL = "FROM_EMAIL";
    private static final String TO_EMAIL = "TO_EMAIL";
    private static final String SUBJECT = "SUBJECT";
    private static final String THEMELESS_FTL = "/email/%s.ftl";
    private static final String THEMED_FTL = "/email/%s/%s.ftl";
    private Logger log = Logger.getLogger(getClass());
    private JavaMailSender mailSender;

    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public Email create(String templateName) {
        return new EmailImpl(templateName);
    }

    public interface Email {
        Email setFrom(String fromEmail);

        Email setTo(String toEmail);

        Email setSubject(String subject);

        Email set(String key, Object value);

        void send();
    }

    private class EmailImpl implements Email {
        private static final String DIVIDER = "\n.\n";
        private Map<String, Object> model = new TreeMap<String, Object>();
        private String templateName;
        private final String portalTheme = "theme/" + ThemeInterceptor.getTheme().getName();

        private EmailImpl(String templateName) {
            this.templateName = templateName;
        }

        public Email setFrom(String fromEmail) {
            return set(FROM_EMAIL, fromEmail);
        }

        public Email setTo(String toEmail) {
            return set(TO_EMAIL, toEmail);
        }

        public Email setSubject(String subject) {
            return set(SUBJECT, subject);
        }

        public Email set(String key, Object value) {
            model.put(key, value);
            return this;
        }

        public void send() {
            try {
                String themedResource = String.format(THEMED_FTL, portalTheme, templateName);
                InputStream inputStream = getClass().getResourceAsStream(themedResource);
                if (inputStream == null) {
                    String themelessResource = String.format(THEMELESS_FTL, templateName);
                    log.info(String.format("Did not find %s, defaulting to %s", themedResource, themelessResource));
                    inputStream = getClass().getResourceAsStream(themelessResource);
                }
                if (inputStream == null) {
                    throw new IllegalStateException(String.format("No template found named %s in theme %s", templateName, portalTheme));
                }
                String fileString = IOUtils.toString(inputStream, "UTF-8");
                int divider = fileString.indexOf(DIVIDER);
                if (divider > 0) {
                    String propertiesString = fileString.substring(0, divider);
                    for (String line : propertiesString.split("\n")) {
                        int equals = line.indexOf("=");
                        String key = line.substring(0, equals).trim();
                        String value = line.substring(equals + 1).trim();
                        model.put(key, value);
                    }
                    fileString = fileString.substring(divider + DIVIDER.length());
                }
                Template template = getTemplate(templateName, new StringReader(fileString));
                StringWriter emailContent = new StringWriter();
                template.process(model, emailContent);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject(getString(SUBJECT));
                message.setTo(getString(TO_EMAIL));
                message.setFrom(getString(FROM_EMAIL));
                message.setText(emailContent.toString());
                mailSender.send(message);
            }
            catch (Exception e) {
                log.error("Failed to send email!", e);
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    log.error(MessageFormat.format("{0} = {1}", entry.getKey(), entry.getValue()));
                }
            }
        }

        private String getString(String key) {
            String value = (String) model.get(key);
            if (value == null) {
                throw new IllegalArgumentException(String.format("Model must contain value for %s", key));
            }
            return value;
        }
    }


    private static Template getTemplate(String name, Reader reader) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        return new Template(name, reader, configuration, "UTF-8");
    }
}