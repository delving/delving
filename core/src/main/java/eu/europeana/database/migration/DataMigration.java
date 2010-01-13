/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.database.migration;

import com.thoughtworks.xstream.XStream;
import eu.europeana.database.LanguageDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.Translation;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.List;

import static eu.europeana.database.migration.DataMigration.Table.*;

/**
 * Export and import a collection of tables.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */
public class DataMigration {
    private static final String DIRECTORY = "/tmp/";
    private static final Logger log = Logger.getLogger(DataMigration.class);
    private LanguageDao languageDao;
    private StaticInfoDao staticInfoDao;

    public DataMigration() {
    }

    public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    public void setPartnerDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    private XStream getXStreamInstance(Table table) {
        XStream xstream = new XStream();
        xstream.processAnnotations(new Class[]{table.getClassType()});
        xstream.alias(table.getRootXmlNode(), List.class);
        return xstream;
    }

    // filewriter get list, enum > write to file ; return int nr processed
    public int dumpTable(Table table, List<?> itemList) throws IOException {
        XStream xStream = getXStreamInstance(table);
        String output = xStream.toXML(itemList);
        File outputFile = new File(DIRECTORY + table.getFileName());
        FileUtils.writeStringToFile(outputFile, output, "utf-8");
        return itemList.size();
    }

    // file reader get enum > persist to database ; return int nr processed
    public int readTable(Table table, Reader reader) throws IOException {
        XStream xStream = getXStreamInstance(table);
        List<Object> objects = (List<Object>) xStream.fromXML(reader);
        storeTable(table, objects);
        return objects.size();
    }

    public int readTableFromResource(Table table) throws IOException {
        InputStream is = getClass().getResourceAsStream("/tables/" + table.getFileName());
        Reader reader = new InputStreamReader(is, "utf-8");
        return readTable(table, reader);
    }

    private void storeTable(Table table, List<Object> objects) {
        for (Object object : objects) {
            switch (table) {
                case CONTRIBUTORS:
                    staticInfoDao.saveContributor((Contributor) object);
                    break;
                case PARTNERS:
                    staticInfoDao.savePartner((Partner) object);
                    break;
                case TRANSLATION_KEYS:
                    Translation translation = (Translation) object;
                    languageDao.setTranslation(translation.getMessageKey().getKey(), translation.getLanguage(), translation.getValue());
                    break;
                case STATIC_PAGE:
                    StaticPage page = (StaticPage) object;
                    staticInfoDao.setStaticPage(page.getPageType(), page.getLanguage(), page.getContent());
                    break;
            }
        }
    }

    public enum Table {
        STATIC_PAGE("static-pages.xml", StaticPage.class, "StaticPages"),
        TRANSLATION_KEYS("translation-keys.xml", Translation.class, "TranslationKeys"),
        CONTRIBUTORS("contributors.xml", Contributor.class, "Contributors"),
        PARTNERS("partners.xml", Partner.class, "Partners");

        private String fileName;
        private Class classType;
        private String rootXmlNode;

        private Table(String fileName, Class classType, String rootXmlNode) {
            this.fileName = fileName;
            this.classType = classType;
            this.rootXmlNode = rootXmlNode;
        }

        public String getFileName() {
            return fileName;
        }

        public Class getClassType() {
            return classType;
        }

        public String getRootXmlNode() {
            return rootXmlNode;
        }

    }

    /*
     * This main program is for real migration.  The class is also used when setting up for tests
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/core-application-context.xml",
        });
        StaticInfoDao staticInfoDao = (StaticInfoDao) context.getBean("staticInfoDao");
        LanguageDao languageDao = (LanguageDao) context.getBean("languageDao");
        DataMigration migration = new DataMigration();

        migration.setPartnerDao(staticInfoDao);
        if (args.length == 1 && args[0].equalsIgnoreCase("import")) {
            for (Table table : Table.values()) {
                File file = new File(DIRECTORY + table.getFileName());
                InputStream is = new FileInputStream(file);
                Reader reader = new InputStreamReader(is, "utf-8");
                migration.readTable(table, reader);
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("export")) {
            migration.dumpTable(STATIC_PAGE, staticInfoDao.getAllStaticPages());
            migration.dumpTable(TRANSLATION_KEYS, languageDao.getAllTranslationMessages());
            migration.dumpTable(CONTRIBUTORS, staticInfoDao.getAllContributors());
            migration.dumpTable(PARTNERS, staticInfoDao.getAllPartnerItems());
        } else {
            throw new Exception("Needs parameter:  import|export");
        }
    }
}
