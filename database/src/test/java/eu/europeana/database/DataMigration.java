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

package eu.europeana.database;

import com.thoughtworks.xstream.XStream;
import static eu.europeana.database.DataMigration.MigrationTable.*;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.Translation;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * Export and import a collection of tables.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class DataMigration {
    private static final String DIRECTORY = "/tmp/";
    private static final Logger log = Logger.getLogger(DataMigration.class);
    private MessageDao messageDao;
    private PartnerDao partnerDao;

    public DataMigration() {
    }

    public void setMessageDao(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public void setPartnerDao(PartnerDao partnerDao) {
        this.partnerDao = partnerDao;
    }

    private XStream getXStreamInstance(MigrationTable table) {
        XStream xstream = new XStream();
        xstream.processAnnotations(new Class[]{table.getClassType()});
        xstream.alias(table.getRootXmlNode(), List.class);
        return xstream;
    }

    // filewriter get list, enum > write to file ; return int nr processed
    public int dumpTable(MigrationTable table, List<?> itemList) throws IOException {
        XStream xStream = getXStreamInstance(table);
        String output = xStream.toXML(itemList);
        File outputFile = new File(DIRECTORY + table.getFileName());
        FileUtils.writeStringToFile(outputFile, output, "utf-8");
        return itemList.size();
    }

    // file reader get enum > persist to database ; return int nr processed
    public int readTable(MigrationTable table, Reader reader) throws IOException {
        XStream xStream = getXStreamInstance(table);
        String stringReadFromFile = FileUtils.readFileToString(new File(DIRECTORY + table.getFileName()), "utf-8");
        List<Object> objects = (List<Object>) xStream.fromXML(reader);
        storeTable(table, objects);
        return objects.size();
    }

    private void storeTable(MigrationTable table, List<Object> objects) {
        for (Object object : objects) {
            switch (table) {
                case CONTRIBUTORS:
                    partnerDao.saveContributor((Contributor) object);
                    break;
                case PARTNERS:
                    partnerDao.savePartner((Partner) object);
                    break;
                case TRANSLATION_KEYS:
                    Translation translation = (Translation) object;
                    messageDao.setTranslation(translation.getMessageKey().getKey(), translation.getLanguage(), translation.getValue());
                    break;
                case STATIC_PAGE:
                    StaticPage page = (StaticPage) object;
                    messageDao.setStaticPage(page.getPageType(), page.getLanguage(), page.getContent());
                    break;
            }
        }
    }

    public enum MigrationTable {
        STATIC_PAGE("static-pages.xml", StaticPage.class, "StaticPages"),
        TRANSLATION_KEYS("translation-keys.xml", Translation.class, "TranslationKeys"),
        CONTRIBUTORS("contributors.xml", Contributor.class, "Contributors"),
        PARTNERS("partners.xml", Partner.class, "Partners");

        private String fileName;
        private Class classType;
        private String rootXmlNode;

        private MigrationTable(String fileName, Class classType, String rootXmlNode) {
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

    /**
     * This main program is for real migration.  The class is also used when setting up for tests
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/default-datasource.xml",
                "/database-application-context.xml",
        });
        MessageDao messageDao = (MessageDao) context.getBean("messageDao");
        PartnerDao partnerDao = (PartnerDao) context.getBean("partnerDao");
        DataMigration migration = new DataMigration();
        migration.setMessageDao(messageDao);
        migration.setPartnerDao(partnerDao);
        if (args.length == 1 && args[0].equalsIgnoreCase("import")) {
            for (MigrationTable table : MigrationTable.values()) {
                File file = new File(DIRECTORY + table.getFileName());
                InputStream is = new FileInputStream(file);
                Reader reader = new InputStreamReader(is, "utf-8");
                migration.readTable(table, reader);
            }
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("export")) {
            migration.dumpTable(STATIC_PAGE, messageDao.getAllStaticPages());
            migration.dumpTable(TRANSLATION_KEYS, messageDao.getAllTranslationMessages());
            migration.dumpTable(CONTRIBUTORS, partnerDao.getAllContributorItems());
            migration.dumpTable(PARTNERS, partnerDao.getAllPartnerItems());
        }
        else {
            throw new Exception("Needs parameter:  import|export");
        }
    }
}
