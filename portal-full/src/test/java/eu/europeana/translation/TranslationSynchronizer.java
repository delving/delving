/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.translation;

import eu.europeana.database.MessageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * move the existing message bundle to the database
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public class TranslationSynchronizer {
    private static final Logger LOG = Logger.getLogger(TranslationSynchronizer.class);
    private static final Pattern FILE_PATTERN = Pattern.compile("messages_(..)\\.properties");
    private static final Pattern AVOID_PATTERN = Pattern.compile("messages_xx\\.properties");
    private static Map<Language, File> messageFiles = new HashMap<Language, File>();

    private static void fillMessageFiles(File directory) {
        for (File propertyFile : directory.listFiles()) {
            if (AVOID_PATTERN.matcher(propertyFile.getName()).matches()) continue;
            Matcher matcher = FILE_PATTERN.matcher(propertyFile.getName());
            if (matcher.matches()) {
                Language language = Language.findByCode(matcher.group(1));
                messageFiles.put(language, propertyFile);
            }
        }
    }

//    private static void messagesToDatabase(MessageDao messageDao) throws IOException {
//        int count = 0;
//        for (Map.Entry<Language, File> entry : messageFiles.entrySet()) {
//            count++;
//            LOG.info("To database: " + count + "/" + messageFiles.size() + "  " + entry.getKey() + " - " + entry.getValue());
//            Properties properties = new Properties();
//            properties.load(new InputStreamReader(new FileInputStream(entry.getValue()), "UTF-8"));
//            for (Object keyObject : properties.keySet()) {
//                String key = (String) keyObject;
//                String value = properties.getProperty(key);
//                messageDao.setTranslation(key, entry.getKey(), value);
//            }
//        }
//    }
//
    private static void databaseToMessages(MessageDao messageDao) throws IOException {
        Map<Language, Map<String,String>> props = new HashMap<Language, Map<String,String>>();
        for (String key : messageDao.fetchMessageKeyStrings()) {
            MessageKey messageKey = messageDao.fetchMessageKey(key);
            for (Translation translation: messageKey.getTranslations()) {
                Map<String,String> properties = props.get(translation.getLanguage());
                if (properties == null) {
                    props.put(translation.getLanguage(), properties = new TreeMap<String,String>());
                }
                properties.put(key, translation.getValue());
            }
        }
        int count = 0;
        for (Map.Entry<Language, File> entry : messageFiles.entrySet()) {
            count++;
            LOG.info("To message file: " + count + "/" + messageFiles.size() + "  " + entry.getKey() + " - " + entry.getValue());
            Map<String,String> properties = props.get(entry.getKey());
            if (properties == null) {
                LOG.warn("No properties for "+entry.getKey());
            }
            else {
                Writer out = new OutputStreamWriter(new FileOutputStream(entry.getValue()), "UTF-8");
                out.write("From database, dumped on "+(new Date()));
                for (Map.Entry<String,String> property : properties.entrySet()) {
                    out.write(property.getKey()+" = "+property.getValue()+"\n");
                }
                out.close();
            }
        }
    }

    private static File selectDirectory() throws IOException {
        File directoryA = new File("./portal-full/src/main/webapp/WEB-INF/classes");
        File directoryB = new File("./src/main/webapp/WEB-INF/classes");
        File directory;
        if (directoryA.exists()) {
            directory = directoryA;
        }
        else if (directoryB.exists()) {
            directory = directoryB;
        }
        else {
            throw new IOException("Cannot find messages directory in the expected places");
        }
        return directory;
    }

    private static void usage() {
        LOG.error("Command line parameter required: 'messages-to-database' or 'database-to-messages'.");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            usage();
            return;
        }
        if (fileLoader(args)) return;
        System.exit(0);
    }

    public static boolean fileLoader(String[] args) throws IOException {
        boolean m2db = args[0].equals("messages-to-database");
        boolean db2m = args[0].equals("database-to-messages");
        if (!(m2db || db2m)) {
            usage();
            return true;
        }
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/database-application-context.xml",
                "/application-context.xml"
        });
        MessageDao messageDao = (MessageDao) context.getBean("messageDao");
        File directory = selectDirectory();
        fillMessageFiles(directory);
        if (m2db) {
            throw new RuntimeException("sorry, out of order");
//            messagesToDatabase(messageDao);
        }
        else {
            databaseToMessages(messageDao);
        }
        return false;
    }

}
