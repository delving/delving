package eu.europeana.database.dao.fixture;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Tools to put some data into the database for testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UserFixture {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public User createUser(String who) {
        User user = new User(null, "user-"+who, who+"@email.com", "password-"+who, "First Name "+who, "Last Name "+who, "", "", "", false, Role.ROLE_USER, true);
        entityManager.persist(user);
        return user;
    }

    @Transactional
    public User addSavedSearch(User user, String query) {
        user = entityManager.merge(user);
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setDateSaved(new Date());
        savedSearch.setLanguage(Language.AFA);
        savedSearch.setQuery(query);
        savedSearch.setQueryString(query+" string");
        savedSearch.setUser(user);
        user.getSavedSearches().add(savedSearch);
        return user;
    }

}
