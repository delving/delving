package eu.europeana.database.dao;

import eu.europeana.database.DataMigration;
//import eu.europeana.database.MessageDao;
import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.integration.DaoMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Locale;

/**
 * @author todo insert: "name" <email>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml"
})


public class TestLanguageDao {

    @Autowired
   // private MessageDao messageDao; // todo: should be LanguageDao
    private LanguageDao languageDao; // todo: should be LanguageDao

    private DaoMessageSource daoMessageSource = new DaoMessageSource();

    @Before
    public void prepare() throws IOException {
        DataMigration migration = new DataMigration();
        migration.setLanguageDao(languageDao);
        migration.readTableFromResource(DataMigration.Table.TRANSLATION_KEYS);
        //daoMessageSource.setMessageDao(languageDao);
        daoMessageSource.setLanguageDao(languageDao);
    }

    @Test
    public void zoek() throws Exception {
        //todo: finish this test
        Locale nl = new Locale(Language.NL.getCode());
        String zoek = daoMessageSource.getMessage("Search_t", null, nl);
        junit.framework.Assert.assertEquals("Zoek", zoek);
    }

// todo: thise methods must be tested here
//    EnumSet<Language> getActiveLanguages();
//    void setLanguageActive(Language language, boolean active);
//    void addMessagekey(String key);
//    void removeMessageKey(String key);
//    Translation setTranslation(String key, Language language, String value);
//    List<String> fetchMessageKeyStrings();
//    MessageKey fetchMessageKey(String key);
//    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);

}