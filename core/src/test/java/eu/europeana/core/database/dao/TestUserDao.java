package eu.europeana.core.database.dao;

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.*;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.fixture.DatabaseFixture;
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

import static junit.framework.Assert.*;

/**
 * Test the UserDao methods
 *
 * @author "Gerald de Jong" <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/core-application-context.xml",
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
        log.info("User 10: " + users.get(10).getEmail());
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
        assertNotNull(user25);
        log.info("Found " + user25.getFirstName());
        assertEquals(1, user25.getSavedSearches().size());
        // test remove retrievedSavedSearch
        final SavedSearch retrievedSavedSearch = user25.getSavedSearches().get(0);
        user25 = userDao.removeSavedSearch(retrievedSavedSearch.getId());
        assertEquals(0, user25.getSavedSearches().size());
        log.info("User.savesSearch.size: " + user25.getSavedSearches().size());
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
        EuropeanaId id7 = databaseFixture.fetchEuropeanaId(europeanaIds.get(7).getId());
        assertEquals(1, id7.getSocialTags().size());
        log.info("tag=" + id7.getSocialTags().get(0).getTag());
        List<UserDao.TagCount> tagCounts = userDao.getSocialTagCounts("Number");
        assertEquals(1, tagCounts.size());
    }

    @Test
    public void userChanges() {
        User user51 = users.get(51);
        user51.setLanguages("languages");
        user51 = userDao.updateUser(user51);
        assertEquals("languages", user51.getLanguages());
        user51.setRole(Role.ROLE_ADMINISTRATOR);
        user51 = userDao.updateUser(user51);
        assertEquals(Role.ROLE_ADMINISTRATOR, user51.getRole());
    }

    @Test
    public void removeUser() {
        userDao.removeUser(users.get(37));
        List<User> remaining = userDao.fetchUsers("Gumby");
        assertEquals(99, remaining.size());
        remaining = userDao.fetchUsers("Gumby3");
        assertEquals(10, remaining.size()); // Gumby3 and Gumby3?
        assertFalse(userDao.userNameExists("user-Gumby37"));
        assertTrue(userDao.userNameExists("user-Gumby38"));
    }

    @Test
    public void fetchByEmail() {
        assertNotNull(userDao.fetchUserByEmail("Gumby29@email.com"));
        assertNull(userDao.fetchUserByEmail("gumby29@email.com"));
        assertNull(userDao.fetchUserByEmail("pokey29@email.com"));
    }
}