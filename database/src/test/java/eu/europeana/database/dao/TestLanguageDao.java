package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author "Gerald de Jong" <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml"
})


public class TestLanguageDao {

    private String keys[] = {"Nicola", "Cesare", "Carlo"};
    private String translateFrKeys[] = {"Nicolas", "Cesar", "Charles"};
    private Logger log = Logger.getLogger(getClass());

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
        assertTrue(activeLanguages.contains(wasInactive));
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
        assertTrue(!activeLanguages.contains(wasActive));
    }

    @Test
    public void messageKeyAddFetchRemove() throws Exception {
        languageDao.addMessagekey(keys[0]);
        MessageKey messageKey = languageDao.fetchMessageKey(keys[0]);
        assertNotNull(messageKey);
        assertEquals(messageKey.getKey(), keys[0]);
        languageDao.removeMessageKey(keys[0]);
        assertNull(languageDao.fetchMessageKey(keys[0]));
    }

    @Test
    public void fetchMessageKeyStrings() throws Exception {
        for (String key : keys) {
            languageDao.addMessagekey(key);
        }
        List<String> messageKeys = languageDao.fetchMessageKeyStrings();
        assertNotNull(messageKeys);
        assertEquals(messageKeys.size(), keys.length);
        for (String key : keys) {
            assertTrue(messageKeys.contains(key));
        }

        for (String key : keys) {
            languageDao.removeMessageKey(key);
        }

        messageKeys = languageDao.fetchMessageKeyStrings();
        assertNotNull(messageKeys);
        for (String key : keys) {
            assertFalse(messageKeys.contains(key));
        }
    }


    @Test
    public void setAndFetchTranslation() throws Exception {
        Set<String> languageCodes = new HashSet<String>();
        languageCodes.add(Language.FR.getCode());

        languageDao.setTranslation(keys[0], Language.FR, translateFrKeys[0]);
        Map<String, List<Translation>> translations = languageDao.fetchTranslations(languageCodes);
        assertNotNull(translations);
        assertTrue(translations.containsKey(keys[0]));
        List<Translation> translationsList = translations.get(keys[0]);
        for (Translation translation : translationsList) {
            if (translation.getMessageKey().getKey().equals(keys[0]) && translation.getLanguage().equals(Language.FR)) {
                assertTrue(translation.getValue().equals(translateFrKeys[0]));
                break;
            }

        }
    }

    @Test
    public void nonexistentKey() throws Exception {
        MessageKey messageKey = languageDao.fetchMessageKey("some.nonexistent.key");
        assertNull(messageKey);
    }
}