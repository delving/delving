package eu.europeana.database.integration;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use the message dao to provide a spring message source
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DaoMessageSource extends AbstractMessageSource {
    private long maximumAge = 60000L;

    private LanguageDao languageDao;
    private Map<String, Map<Language, CacheValue>> cache = new ConcurrentHashMap<String, Map<Language, CacheValue>>();

     public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }
    public void setMaximumAge(long maximumAge) {
        this.maximumAge = maximumAge;
    }

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        return getFromCache(key, locale).getMessageFormat();
    }

    @Override
    protected String resolveCodeWithoutArguments(String key, Locale locale) {
        return getFromCache(key, locale).getString();
    }

    private CacheValue getFromCache(String key, Locale locale) {
        Map<Language, CacheValue> keyTranslations = cache.get(key);
        if (keyTranslations == null) {
            keyTranslations = fetchTranslations(key, locale);
            cache.put(key, keyTranslations);
        }
        Language language = getLanguage(locale);
        CacheValue cacheValue = keyTranslations.get(language);
        if (cacheValue.isOlderThan(maximumAge)) {
            cache.remove(key);
        }
        return cacheValue;
    }

    private Language getLanguage(Locale locale) {
        String languageCode = locale.getLanguage().toUpperCase();
        Language language = Language.valueOf(languageCode);
        if (language == null) {
            language = Language.EN;
        }
        return language;
    }

    private Map<Language, CacheValue> fetchTranslations(String key, Locale locale) {
        Map<Language, CacheValue> translations = new HashMap<Language, CacheValue>();
       // MessageKey messageKey = messageDao.fetchMessageKey(key);
        MessageKey messageKey = languageDao.fetchMessageKey(key);
        for (Translation translation : messageKey.getTranslations()) {
            translations.put(translation.getLanguage(), new CacheValue(locale, translation.getValue()));
        }
        return translations;
    }

    private class CacheValue {
        private long created = System.currentTimeMillis();
        private MessageFormat messageFormat;
        private Locale locale;
        private String string;

        private CacheValue(Locale locale, String string) {
            this.locale = locale;
            this.string = string;
        }

        public boolean isOlderThan(long period) {
            return (System.currentTimeMillis() - created) > period;
        }

        public MessageFormat getMessageFormat() {
            if (messageFormat == null) {
                messageFormat = createMessageFormat(string, locale);
            }
            return messageFormat;
        }

        public String getString() {
            return string;
        }
    }
}
