package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.LanguageActivation;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@SuppressWarnings("unchecked")
public class LanguageDaoImpl implements LanguageDao {

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
}
