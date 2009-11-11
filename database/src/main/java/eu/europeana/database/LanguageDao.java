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
 * @author Nicola Aloia
 */

public interface LanguageDao {

    /**
     * Get all Active languages
     *
     * @return an EnumSet of Active languages
     */
    EnumSet<Language> getActiveLanguages();

    void setLanguageActive(Language language, boolean active);

    /**
     * Inizialize and return an instance of the Translation class
     *
     * @param key      - String
     * @param language an instance of Language class
     * @param value    - String
     * @return an instance of the Translation class
     */
    Translation setTranslation(String key, Language language, String value);

    /**
     * Get all Message Keys
     *
     * @return a List of String containing all the Message keys
     */
    List<String> fetchMessageKeyStrings();

    /**
     * Get the MessageKey for the given key
     *
     * @param key - String
     * @return an instance of MessageKey
     */
    MessageKey fetchMessageKey(String key);

    /**
     * Translate the given Set of languageCodes
     *
     * @param languageCodes - a Set of String codes
     * @return a MAP containing String Language code with relative translation.
     */
    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);


    /**
     * Add the given key
     *
     * @param key - String
     */
    void addMessagekey(String key);

    /**
     * Remove the given key
     *
     * @param key - String
     */
    void removeMessageKey(String key);


}
