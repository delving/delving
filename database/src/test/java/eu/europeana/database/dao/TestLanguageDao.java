package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

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

    @Test
    public void messageKeyAddFetchRemove() throws Exception {
        String key = "__Nicola";
        languageDao.addMessagekey(key);
        MessageKey messageKey = languageDao.fetchMessageKey(key);
        Assert.notNull(messageKey);
        languageDao.removeMessageKey(key);
        messageKey = languageDao.fetchMessageKey(key);
        Assert.isNull(messageKey);
    }

    @Test
    public void fetchMessageKeyStrings() throws Exception {
        String keys[] = {"__Nicola1", "__Nicola2", "__Nicola3"};

        for (String key : keys) {
            languageDao.addMessagekey(key);
        }
        List<String> messageKeys = languageDao.fetchMessageKeyStrings();
        Assert.notNull(messageKeys);
        Assert.isTrue(messageKeys.size() >= keys.length);
        for (String key : keys) {
            Assert.isTrue(messageKeys.contains(key));
        }

        for (String key : keys) {
            languageDao.removeMessageKey(key);
        }

        messageKeys = languageDao.fetchMessageKeyStrings();
        Assert.notNull(messageKeys);
        for (String key : keys) {
            Assert.isTrue(!messageKeys.contains(key));
        }
    }


    @Test
    public void setAndFetchTranslation() throws Exception {
        Set<String> languageCodes = new HashSet<String>();
        languageCodes.add(Language.FR.getCode());
        String key = "Nicola";
        String value = "Nicolas";
        languageDao.setTranslation(key, Language.FR, value);
        Map<String, List<Translation>> translations = languageDao.fetchTranslations(languageCodes);
        Assert.notNull(translations);
        Assert.isTrue(translations.containsKey(key));
        List<Translation> translationsList = translations.get(key);
        for (Translation translation : translationsList) {
            if (translation.getMessageKey().getKey().equals(key) && translation.getLanguage().equals(Language.FR)) {
                Assert.isTrue(translation.getValue().equals(value));
                break;
            }

        }
    }


}