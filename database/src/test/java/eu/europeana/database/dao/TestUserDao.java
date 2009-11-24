package eu.europeana.database.dao;

import eu.europeana.database.UserDao;
import eu.europeana.database.dao.fixture.DatabaseFixture;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.query.DocType;
import static junit.framework.Assert.*;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Test the UserDao methods
 *
 * @author "Gerald de Jong" <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml",
        "/test-application-context.xml"
})

@Transactional
public class TestUserDao {
    private Logger log = Logger.getLogger(TestUserDao.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private DatabaseFixture databaseFixture;

    private List<User> users;
    private List<EuropeanaId> europeanaIds;

    @Before
    public void prepare() throws IOException {
        users = databaseFixture.createUsers("Gumby", 100);
        log.info("User 10: "+users.get(10).getEmail());
        europeanaIds = databaseFixture.createEuropeanaIds("Test Collection", 10);
    }

    @Test
    public void authenticate() {
        User authenticated = userDao.authenticateUser("Gumby89@email.com", "password-Gumby89");
        assertNotNull(authenticated);
        User refused = userDao.authenticateUser("Gumby89@email.com", "password-Gumby88");
        assertNull(refused);
        // give it that password
        authenticated.setPassword("password-Gumby88");
        userDao.updateUser(authenticated);
        // now the tables are turned!
        authenticated = userDao.authenticateUser("Gumby89@email.com", "password-Gumby88");
        assertNotNull(authenticated);
        refused = userDao.authenticateUser("Gumby89@email.com", "password-Gumby89");
        assertNull(refused);
    }

    @Test
    public void testSavedSearch() {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setDateSaved(new Date());
        savedSearch.setLanguage(Language.AFA);
        savedSearch.setQuery("query");
        savedSearch.setQueryString("querystring");
        User user25 = userDao.addSavedSearch(users.get(25), savedSearch);
        log.info("User.savesSearch.size: "+user25.getSavedSearches().size());
        assertNotNull(user25);
        log.info("Found "+user25.getFirstName());
        assertEquals(1, user25.getSavedSearches().size());
    }

    @Test
    public void testTags() {
        SocialTag socialTag = new SocialTag();
        socialTag.setDateSaved(new Date());
        socialTag.setDocType(DocType.SOUND);
        socialTag.setEuropeanaId(europeanaIds.get(7));
        socialTag.setTag("Number Seven");
        socialTag.setEuropeanaObject("http://europeana.obect.pretend/");
        User user49 = userDao.addSocialTag(users.get(49), socialTag);
        assertEquals(1, user49.getSocialTags().size());
        //todo:
//        EuropeanaId id7 = databaseFixture.fetch(EuropeanaId.class, 7L);
//        assertEquals(1, id7.getSocialTags().size());
//        assertEquals(1, userDao.getSocialTagCounts("Number").size());
    }

// todo: these methods must still be tested
//    User fetchUserByEmail(String email);
//    User addUser(User user);
//    void removeUser(User user);
//    void updateUser(User user);
//    User refreshUser(User user);
//    boolean userNameExists(String userName);
//    User remove(User user, Class<?> clazz, Long id);
//    List<User> fetchUsers(String pattern);
//    User fetchUserWhoPickedCarouselItem(String europeanaUri);
//    User fetchUserWhoPickedEditorPick(String query);
//    void setUserEnabled(Long userId, boolean enabled);
//    void setUserToAdministrator(Long userId, boolean administrator);
//    void markAsViewed(String europeanaUri);
//    User addSocialTag(User user, SocialTag socialTag);
//    List<TagCount> getSocialTagCounts(String query);
//    User addSavedItem(User user, SavedItem savedItem, String europeanaUri);
//    void setUserRole(Long userId, Role role);
//    void removeUser(Long userId);
//    User fetchUser(Long userId);
//    void setUserProjectId(Long userId, String projectId);
//    void setUserProviderId(Long userId, String providerId);
//    void setUserLanguages(Long userId, String languages);
//    List<SavedItem> fetchSavedItems(Long userId);
//    SavedItem fetchSavedItemById(Long id);
//    List<SavedSearch> fetchSavedSearches(Long userId);
//    SavedSearch fetchSavedSearchById(Long id);

}