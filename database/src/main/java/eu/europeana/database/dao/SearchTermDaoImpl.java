package eu.europeana.database.dao;

import eu.europeana.database.SearchTermDao;
import eu.europeana.database.domain.SearchTerm;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author vitali
 */
public class SearchTermDaoImpl implements SearchTermDao {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public List<SearchTerm> getAllSearchTerms() {
        Query q = entityManager.createQuery("select st from SearchTerm st");
        List searchTerms = q.getResultList();
        return (List<SearchTerm>) searchTerms;
    }
}
