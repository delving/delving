package eu.europeana.database.dao.fixture;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tools to put some data into the database for testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UserFixture {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public List<User> createUsers(String who, int count) {
        List<User> users = new ArrayList<User>();
        while (count-- > 0) {
            User user = new User(
                    null,
                    "user-"+who+count,
                    who+count+"@email.com",
                    "password-"+who+count,
                    "First Name "+who+count,
                    "Last Name "+who+count,
                    "", "", "", false,
                    Role.ROLE_USER, true
            );
            entityManager.persist(user);
            users.add(user);
        }
        return users;
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
