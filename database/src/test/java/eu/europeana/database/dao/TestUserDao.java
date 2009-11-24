package eu.europeana.database.dao;

import eu.europeana.database.UserDao;
import eu.europeana.database.dao.fixture.UserFixture;
import eu.europeana.database.domain.User;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

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

public class TestUserDao {
    private Logger log = Logger.getLogger(TestUserDao.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserFixture userFixture;

    @Before
    public void prepare() throws IOException {
        User user = userFixture.createUser("Gumby");
        log.info("User: "+user.getEmail());
        user = userFixture.addSavedSearch(user, "save this!");
        log.info("User.savesSearch.size: "+user.getSavedSearches().size());
    }

    @Test
    public void testFixture() {
        User user = userDao.fetchUserByEmail("Gumby@email.com");
        Assert.assertNotNull(user);
        log.info("Found "+user.getFirstName());
        Assert.assertEquals(1, user.getSavedSearches().size());
    }

// todo: thise methods must be tested
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
//    User addSavedSearch(User user, SavedSearch savedSearch);
//    User fetchUser(String email, String password);
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