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

    private String keys[] = {"Nicola", "Cesare", "Carlo"};
    private String translateFrKeys[] = {"Nicolas", "Cesar", "Charle"};

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

        languageDao.addMessagekey(keys[0]);
        MessageKey messageKey = languageDao.fetchMessageKey(keys[0]);
        Assert.notNull(messageKey);
        languageDao.removeMessageKey(keys[0]);
        messageKey = languageDao.fetchMessageKey(keys[0]);
        Assert.isNull(messageKey);
    }

    @Test
    public void fetchMessageKeyStrings() throws Exception {


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

        languageDao.setTranslation(keys[0], Language.FR, translateFrKeys[0]);
        Map<String, List<Translation>> translations = languageDao.fetchTranslations(languageCodes);
        Assert.notNull(translations);
        Assert.isTrue(translations.containsKey(keys[0]));
        List<Translation> translationsList = translations.get(keys[0]);
        for (Translation translation : translationsList) {
            if (translation.getMessageKey().getKey().equals(keys[0]) && translation.getLanguage().equals(Language.FR)) {
                Assert.isTrue(translation.getValue().equals(translateFrKeys[0]));
                break;
            }

        }
    }


}