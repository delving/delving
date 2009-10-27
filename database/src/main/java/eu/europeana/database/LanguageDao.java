package eu.europeana.database;

import eu.europeana.database.domain.Language;

import java.util.EnumSet;

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

    // todo: add these (implementations in MessageDaoImpl)
//    Translation setTranslation(String key, Language language, String value);
//    List<String> fetchMessageKeyStrings();
//    MessageKey fetchMessageKey(String key);
//    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);

}
