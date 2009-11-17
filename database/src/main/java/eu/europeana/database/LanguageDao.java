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
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

/**
 * This interface is used to manage messages in different languages.
 * The messages are handled through the persistent class {@link Translation}, which contains the following fields:
 * <ol>
 * <li>id - the unique identifier of the message </li>
 * <li>language - a three char code of the language, conform to ISO 3166  </li>
 * <li>value - the String value of the message  </li>
 * <li>messageLeyId - the identifier of a mnemonic code </li>      todo: Is Ok?
 * </0l>
 */
public interface LanguageDao {

    /**
     * Get an enumeration of all the active languages. All the ISO 3166 code languages
     * are stored in the database, with a boolean value (active/not active).
     *
     * @return an EnumSet of the active languages.
     * @see   {@link Language}
     */
    EnumSet<Language> getActiveLanguages();

    /**
     * Active/Deactivate a Language, based on the given boolean parameter.
     * @param language an instance of the {@link Language} class.
     * @param active a boolean value.
     * @see   {@link Language}
     */
    void setLanguageActive(Language language, boolean active);

    /**
     * Add an entry to the persistent class {@link Translation}.
     *
     * @param key      - String
     * @param language an instance of {@link Language} class
     * @param value    - String
     * @return an instance of the {@link Translation} class
     * @see   {@link Language}
     * @see   {@link Translation}
     */
    Translation setTranslation(String key, Language language, String value);

    /**
     * Get all the Message Keys                              todo: clarify the meaning of MessageKey
     *
     * @return a List of String containing all the Message keys
     */
    List<String> fetchMessageKeyStrings();

    /**
     * Get the MessageKey for the given mnemonic value of the key message. todo: ???
     *
     * @param key - String - the mnemonic value of the key message.
     * @return an instance of {@link MessageKey}
     * @see   {@link MessageKey}
     */
    MessageKey fetchMessageKey(String key);

    /**
     * Get a Map containing for each given language code the corresponding
     * instance of the {@link Translation} class.
     *
     * @param languageCodes - a Set of ISO 3166 code languages.
     * @return a MAP containing String Language code with relative translation.
     * @see    {@link Translation}
     */
    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);


    /**
     * Persist an instance of the  {@link MessageKey} class for the given mnemonic key value.
     *
     * @param key - String the mnemonic key value.
     */
    void addMessagekey(String key);

    /**
     * Remove the instance of the  {@link MessageKey} class for the given mnemonic key value.
     *
     * @param key - String the mnemonic key value.
     */
    void removeMessageKey(String key);


    /**
     * Get all instances of the {@link MessageKey} class. The MessageKey class is a persistent class
     * to manage messages among different languages.
     *
     * @return a List containing all the {@link MessageKey} items
     * @see    MessageKey
     */
    List<MessageKey> getAllTranslationMessages();

}
