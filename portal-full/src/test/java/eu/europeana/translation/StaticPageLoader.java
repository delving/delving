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
import eu.europeana.database.domain.StaticPageType;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * move the existing message bundle to the database
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public class StaticPageLoader {
    private static final Logger LOG = Logger.getLogger(StaticPageLoader.class);
    private static final Pattern FILE_PATTERN = Pattern.compile("inc_([^_]*)_([^_][^_])\\.ftl");
    private static Map<Key, File> templateFiles = new HashMap<Key, File>();

    private static class Key {
        private StaticPageType pageType;
        private Language language;

        private Key(StaticPageType pageType, Language language) {
            this.pageType = pageType;
            this.language = language;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return language == key.language && pageType == key.pageType;

        }

        @Override
        public int hashCode() {
            int result = pageType != null ? pageType.hashCode() : 0;
            result = 31 * result + (language != null ? language.hashCode() : 0);
            return result;
        }

        public StaticPageType getPageType() {
            return pageType;
        }

        public Language getLanguage() {
            return language;
        }
    }

    private static void fillTemplateFiles(File directory) {
        for (File propertyFile : directory.listFiles()) {
            Matcher matcher = FILE_PATTERN.matcher(propertyFile.getName());
            if (matcher.matches()) {
                StaticPageType pageType = StaticPageType.get(matcher.group(1));
                if (pageType == null) {
                    throw new RuntimeException("Unknown page type: "+matcher.group(1));
                }
                Language language = Language.findByCode(matcher.group(2));
                if (language == null) {
                    throw new RuntimeException("Unknown language: "+matcher.group(2));
                }
                templateFiles.put(new Key(pageType, language), propertyFile);
            }
        }
    }

    private static void templatesToDatabase(MessageDao messageDao) throws IOException {
        int count = 0;
        for (Map.Entry<Key, File> entry : templateFiles.entrySet()) {
            count++;
            LOG.info("To database: " + count + "/" + templateFiles.size() + "  " + entry.getKey() + " - " + entry.getValue());
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getValue()), "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line.trim());
                content.append('\n');
            }
            messageDao.setStaticPage(entry.getKey().getPageType(), entry.getKey().getLanguage(), content.toString());
        }
    }

    private static File selectDirectory() throws IOException {
        File directoryA = new File("portal/src/main/webapp/WEB-INF/templates");
        File directoryB = new File("src/main/webapp/WEB-INF/templates");
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

    public static void fileLoader() throws IOException {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/database-application-context.xml",
                "/application-context.xml"
        });
        MessageDao messageDao = (MessageDao) context.getBean("messageDao");
        File directory = selectDirectory();
        fillTemplateFiles(directory);
        templatesToDatabase(messageDao);
    }

    public static void main(String[] args) throws IOException {
        fileLoader();
        if (args[0] == null || !args[0].equalsIgnoreCase("true")) {
            System.exit(0);
        }
    }

}