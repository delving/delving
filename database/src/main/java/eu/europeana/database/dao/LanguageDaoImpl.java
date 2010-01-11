package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.LanguageActivation;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Translation;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Cesare Concordia <cesare.concordia@isti.cnr.it>
 * @author Nicola Aloia  <nicola.aloia@isti.cnr.it>
 */

@SuppressWarnings("unchecked")
public class LanguageDaoImpl implements LanguageDao {

    private Logger log = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public EnumSet<Language> getActiveLanguages() {
        Query query = entityManager.createQuery("select la from LanguageActivation as la order by la.language");
        List<LanguageActivation> activations = query.getResultList();
        EnumSet<Language> active = EnumSet.noneOf(Language.class);
        for (Language language : Language.values()) {
            LanguageActivation found = null;
            for (LanguageActivation activation : activations) {
                if (activation.getLanguage() == language) {
                    found = activation;
                    break;
                }
            }
            if (found != null) {
                if (found.isActive()) {
                    active.add(found.getLanguage());
                }
            }
            else if (language.isActiveByDefault()) {
                active.add(language);
            }
        }
        return active;
    }

    @Transactional
    public void setLanguageActive(Language language, boolean active) {
        Query query = entityManager.createQuery("select la from LanguageActivation as la where la.language = :language");
        query.setParameter("language", language);
        List<LanguageActivation> activations = query.getResultList();
        if (activations.isEmpty()) {
            entityManager.persist(new LanguageActivation(language, active));
        }
        else if (activations.size() == 1) {
            activations.get(0).setActive(active);
        }
        else {
            throw new RuntimeException("More than one language activation!");
        }
    }

    @Transactional
    public Translation setTranslation(String key, Language language, String value) {
        Query query = entityManager.createQuery("select mk from MessageKey mk where mk.key = :key");
        query.setParameter("key", key);
        List<MessageKey> messageKeys = (List<MessageKey>) query.getResultList();
        MessageKey messageKey;
        Translation translation;
        if (messageKeys.isEmpty()) {
            messageKey = new MessageKey(key);
            translation = messageKey.setTranslation(language, value);
            entityManager.persist(messageKey);
        }
        else {
            messageKey = messageKeys.get(0);
            translation = messageKey.setTranslation(language, value);
        }
        return translation;
    }

    @Transactional
    public List<String> fetchMessageKeyStrings() {
        Query query = entityManager.createQuery("select mk.key from MessageKey mk");
        return (List<String>) query.getResultList();
    }

    @Transactional
    public MessageKey fetchMessageKey(String key) {
        Query query = entityManager.createQuery("select mk from MessageKey mk where mk.key = :key");
        query.setParameter("key", key);
        try {
            MessageKey messageKey = (MessageKey) query.getSingleResult();
            messageKey.getTranslations().size();
            return messageKey;
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes) {
        Map<String, List<Translation>> translations = new TreeMap<String, List<Translation>>();
        for (String languageCode : languageCodes) {
            Query query = entityManager.createQuery("select t from Translation t where t.language = :language");
            Language language = Language.findByCode(languageCode);
            query.setParameter("language", language);
            List<Translation> tlist = (List<Translation>) query.getResultList();
            for (Translation trans : tlist) {
                List<Translation> value = translations.get(trans.getMessageKey().getKey());
                if (value == null) {
                    value = new ArrayList<Translation>();
                    translations.put(trans.getMessageKey().getKey(), value);
                }
                value.add(trans);
            }
        }
        return translations;
    }

    @Transactional
    public void addMessagekey(String key) {
        MessageKey messageKey = new MessageKey(key);
        entityManager.persist(messageKey);
    }

    @Transactional
    public void removeMessageKey(String key) {
        Query query = entityManager.createQuery("select k from MessageKey k where k.key = :key");
        query.setParameter("key", key);
        try {
            MessageKey messageKey = (MessageKey) query.getSingleResult();
            entityManager.remove(messageKey);
        }
        catch (NoResultException e) {
            log.warn("Unable to remove message key " + key);
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<MessageKey> getAllTranslationMessages() {
        Query query = entityManager.createQuery("select trans from Translation as trans");
        return query.getResultList();
    }

}
