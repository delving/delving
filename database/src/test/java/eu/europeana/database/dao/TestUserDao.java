package eu.europeana.database.dao;

import eu.europeana.database.UserDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 *
 * @author todo insert "name" <emeail>
 * @since Mar 18, 2009: 3:29:31 PM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml",
        "/hypersonic-datasource.xml"
})

public class TestUserDao {

    @Autowired
    private UserDao userDao;

    @Before
    public void prepare() throws IOException {
    }

    @Test
    public void test1() {
        Assert.fail();
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