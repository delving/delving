package eu.europeana;

import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Just create a user
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UserFixture {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public User insertUser() {
        User user = createUser("Gumby", "McPokey", "gumby@europeana.eu");
        entityManager.persist(user);
        EuropeanaCollection collection = createEuropeanaCollection();
        entityManager.persist(collection);
        EuropeanaId europeanaId = createEuropeanaId("http://uri", collection);
        entityManager.persist(europeanaId);
        SavedItem savedItem = createSavedItem(europeanaId);
        user.getSavedItems().add(savedItem);
        SocialTag socialTag = createSocialTag(user,europeanaId);
        europeanaId.getSocialTags().add(socialTag);
        user.getSocialTags().add(socialTag);
        SavedSearch savedSearch = createSavedSearch();
        user.getSavedSearches().add(savedSearch);
        return entityManager.merge(user);
    }

    private User createUser(String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserName(firstName.toLowerCase());
        user.setPassword(firstName.toLowerCase());
        user.setEmail(email);
        user.setEnabled(true);
        user.setLastLogin(new Date());
        user.setNewsletter(true);
        user.setRegistrationDate(new Date());
        user.setRole(Role.ROLE_USER);
        return user;
    }


    private SavedItem createSavedItem(EuropeanaId europeanaId) {
        SavedItem savedItem = new SavedItem();
        savedItem.setTitle("Historical Book");
        savedItem.setAuthor("I. Wroteit");
        savedItem.setDateSaved(new Date());
        savedItem.setEuropeanaObject("http://europeana/object");
        savedItem.setEuropeanaId(europeanaId);
        return savedItem;
    }

    private SavedSearch createSavedSearch() {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setDateSaved(new Date());
        savedSearch.setQuery("kultur");
        return savedSearch;
    }

    private SocialTag createSocialTag(User user, EuropeanaId europeanaId) {
        SocialTag socialTag = new SocialTag();
        socialTag.setDateSaved(new Date());
        socialTag.setTag("tag");
        socialTag.setUser(user);
        socialTag.setEuropeanaId(europeanaId);
        return socialTag;
    }

    private EuropeanaCollection createEuropeanaCollection() {
        EuropeanaCollection coll = new EuropeanaCollection();
        coll.setName("CollectThis");
        coll.setFileState(ImportFileState.IMPORTING);
        return coll;
    }

    private EuropeanaId createEuropeanaId(String uri, EuropeanaCollection collection) {
        EuropeanaId id = new EuropeanaId(collection);
        id.setCreated(new Date());
        id.setEuropeanaUri(uri);
        return id;
    }

}
