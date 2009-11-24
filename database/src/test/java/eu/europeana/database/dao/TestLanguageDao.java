package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.EnumSet;

/**
 * @author "Gerald de Jong" <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml"
})


public class TestLanguageDao {

    @Autowired
    private LanguageDao languageDao;

    @Before
    public void prepare() throws IOException {
    }

    @Test
    public void activateLanguage() throws Exception {
        Language wasInactive = null;
        for (Language language : Language.values()) {
            if (!language.isActiveByDefault()) {
                wasInactive = language;
                break;
            }
        }
        languageDao.setLanguageActive(wasInactive, true);
        EnumSet<Language> activeLanguages = languageDao.getActiveLanguages();
        Assert.isTrue(activeLanguages.contains(wasInactive));
    }

    @Test
    public void deactivateLanguage() throws Exception {
        Language wasActive = null;
        for (Language language : Language.values()) {
            if (language.isActiveByDefault()) {
                wasActive = language;
                break;
            }
        }
        languageDao.setLanguageActive(wasActive, false);
        EnumSet<Language> activeLanguages = languageDao.getActiveLanguages();
        Assert.isTrue(!activeLanguages.contains(wasActive));
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