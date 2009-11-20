package eu.europeana.database;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Language class works like an enumeration to record all of the languages supported by
 * the platform, but since we need to be able to enable and disable languages on the fly
 * there is an entity called LanguageActivation which stores exceptions to the default activation
 * status stored for each language in its "activeByDefault" boolean.
 *
 * Further, the langauge dao is responsible for maintaining the i18n key-value mappings normally
 * stored in property files with one for each locale.  Here all of this storage is happening
 * in the database so that people can adjust it using the Dashboard and the result does not
 * require a redeploy.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

public interface LanguageDao {

    /**
     * Get an enumeration of all the active languages. All the ISO 3166 code languages
     * are stored in the database, with a boolean value (active/not active).
     *
     * @return an EnumSet of the active languages.
     * @see {@link Language}
     */

    EnumSet<Language> getActiveLanguages();

    /**
     * Active/Deactivate a Language, based on the given boolean parameter.  This means
     * store in the databse an exception to the default activation
     *
     * @param language an instance of the {@link Language} class.
     * @param active   a boolean value.
     * @see {@link Language}
     */

    void setLanguageActive(Language language, boolean active);

    /**
     * Add an entry to the persistent class {@link Translation}.
     *
     * @param key originating mostly in the page templates, identifies the language-dependent string
     * @param language an instance of {@link Language} class
     * @param value what the key should translate to in the user interface
     * @return an instance of the {@link Translation} class, containing what was set
     */

    Translation setTranslation(String key, Language language, String value);

    /**
     * Fetch a list of all the possible keys for which there are translations
     *
     * @return a List of String containing all the Message keys
     */
    
    List<String> fetchMessageKeyStrings();

    /**
     * Get the MessageKey for the given mnemonic value of the key message. It will contain all its translations.
     *
     * @param key - String - the mnemonic value of the key message.
     * @return an instance of {@link MessageKey}
     * @see {@link MessageKey}
     */

    MessageKey fetchMessageKey(String key);

    /**
     * Get a Map containing for each given language code the corresponding
     * instance of the {@link Translation} class.
     *
     * @param languageCodes - a Set of ISO 3166 code languages.
     * @return a MAP containing String Language code with relative translation.
     * @see {@link Translation}
     */

    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);


    /**
     * Persist an instance of the  {@link MessageKey} class for the given mnemonic key value.  It will start
     * without any translations, since they will be added later.  It waS not necessary to return anything
     * becasue a general fetch of everything follows a call to this.
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
     * to manage messages among different languages.  Note: used in DataMigration only.
     *
     * @return a List containing all the {@link MessageKey} items
     * @see MessageKey
     */

    List<MessageKey> getAllTranslationMessages();

}
