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

package eu.europeana.database.migration;

import com.thoughtworks.xstream.XStream;
import eu.europeana.database.MessageDao;
import eu.europeana.database.PartnerDao;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.Translation;
import static eu.europeana.database.migration.DataMigration.MigrationTable.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 29, 2009: 1:30:37 PM
 */
public class DataMigration {
    private static final Logger log = Logger.getLogger(DataMigration.class);
    private MessageDao messageDao;
    private PartnerDao partnerDao;
    private String importDirectory;

    public DataMigration(String importDirectory) {
        this.importDirectory = importDirectory;
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/database-application-context.xml",
                "/test-application-context.xml"
        });
        messageDao = (MessageDao) context.getBean("messageDao");
        partnerDao = (PartnerDao) context.getBean("partnerDao");
    }

    private XStream getXStreamInstance(MigrationTable table) {
        XStream xstream = new XStream();
        xstream.processAnnotations(new Class[]{table.getClassType()});
        xstream.alias(table.getRootXmlNode(), List.class);
        return xstream;
    }

    // filewriter get list, enum > write to file ; return int nr processed
    public int dumpTableToFile(MigrationTable table, List<?> itemList) throws IOException {
        XStream xStream = getXStreamInstance(table);
        String output = xStream.toXML(itemList);
        File outputFile = new File(importDirectory + table.getFileName());
        FileUtils.writeStringToFile(outputFile, output, "utf-8");
        return itemList.size();
    }

    // file reader get enum > persist to database ; return int nr processed
    public int readTableFromFIle(MigrationTable table) throws IOException {
        XStream xStream = getXStreamInstance(table);
        String stringReadFromFile = FileUtils.readFileToString(new File(importDirectory + table.getFileName()), "utf-8");
        List<Object> objects = (List<Object>) xStream.fromXML(stringReadFromFile);
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
        return objects.size();
    }

    public String getImportDirectory() {
        return importDirectory;
    }

    public int exportTables() throws IOException {
        dumpTableToFile(STATIC_PAGE, messageDao.getAllStaticPages());
        dumpTableToFile(TRANSLATION_KEYS, messageDao.getAllTranslationMessages());
        dumpTableToFile(CONTRIBUTORS, partnerDao.getAllContributorItems());
        dumpTableToFile(PARTNERS, partnerDao.getAllPartnerItems());
        return 4;
    }

    public int importTables() throws IOException {
        int fileProcessed = 0;
        for (MigrationTable migrationTable : values()) {
            readTableFromFIle(migrationTable);
            fileProcessed++;
        }
        return fileProcessed;
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
}
