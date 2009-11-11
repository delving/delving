package eu.europeana.database;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The language enum contains activeByDefault as boolean, but the database
 * is the ultimate authority on which languages are active.
 * 
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface LanguageDao {

    EnumSet<Language> getActiveLanguages();
    void setLanguageActive(Language language, boolean active);

    // todo: add these (implementations in DashboardDaoImpl)
//    void addMessagekey(String key);
//    void removeMessageKey(String key);

    //  added these 
//    Translation setTranslation(String key, Language language, String value);
//    List<String> fetchMessageKeyStrings();
//    MessageKey fetchMessageKey(String key);
//    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);

    Translation setTranslation(String key, Language language, String value);
    List<String> fetchMessageKeyStrings();
    MessageKey fetchMessageKey(String key);
    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);
 

    /**
     *
     * @param key
     */
    void addMessagekey(String key);

    /**
     * 
     * @param key
     */
    void removeMessageKey(String key);


}
