package eu.europeana.database.dao;

import eu.europeana.database.DashboardDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.*;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author todo insert: "name" <email>
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {
//        "/core-application-context.xml"
//})

public class TestDashboardDao {
    private Logger log = Logger.getLogger(getClass());
    // europeanaUri from 92001_Ag_EU_TELtreasures.xml
    public static final String EUROPEANA_URI_1 = "http://www.europeana.eu/resolve/record/92001/79F2A36A85CE59D4343770F4A560EBDF5F207735"; // is orphan
    public static final String EUROPEANA_URI_2 = "http://www.europeana.eu/resolve/record/92001/C4613D0F3AB97C7A1CCD0A20268BBD79E5CAB51F";
    private User user1;
    private User user2;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private UserDao userDao; // todo: eliminate, because it should not be necessary
    @Autowired
    private StaticInfoDao staticInfoDao; // todo: eliminate, because it should not be necessary

    @Test
    public void testNothingButAtLeastThereIsATestMethod() {
        log.info("activate the tests here!");
    }

//    @Before
    public void init() {
        user1 = createUser("test1", "tester1", "test1@tester.com");
        user2 = createUser("test2", "tester2", "test2@tester.com");

    }

//    @Test
    public void testFindOrphanedSavedItem() throws Exception {
        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        assertFalse("Orphan should be false", europeanaId.isOrphan());
        User user = userDao.addUser(user1);
        user = userDao.addSavedItem(user, new SavedItem(), EUROPEANA_URI_2);
        List<SavedItem> savedItems = user.getSavedItems();
        assertTrue(!savedItems.isEmpty());
        europeanaId.setOrphan(true);
        for (SavedItem savedItem : savedItems) {
            assertTrue("Orphan should be true", savedItem.getEuropeanaId().isOrphan());
            assertFalse("Orphan should not have carouselItem", savedItem.hasCarouselItem());
        }

    }

//    @Test
    public void testSavedItemToCarouselItem() throws Exception {
        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        assertFalse("Orphan should be false", europeanaId.isOrphan());
        User user = userDao.addUser(user2);
        user = userDao.addSavedItem(user, new SavedItem(), EUROPEANA_URI_1);
        user = userDao.updateUser(user);
        List<SavedItem> savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        for (SavedItem savedItem : savedItems) {
            assertFalse("SavedItem should have no carousel item", savedItem.hasCarouselItem());
            assertNotNull(savedItem.getId());
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItem.getId());
            Assert.assertTrue(carouselItem.getSavedItem() != null);
            assertTrue("SavedItem should have one carousel item", savedItem.hasCarouselItem());
        }
        savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        for (SavedItem savedItem : savedItems) {
            assertNotNull(savedItem.getId());
            assertTrue("SavedItem should have carousel item", savedItem.hasCarouselItem());
            assertEquals("saved id and referenced id should be the same",
                    savedItem.getTitle(),
                    savedItem.getCarouselItem().getTitle());
            assertNotNull(savedItem.getCarouselItem().getEuropeanaId().isOrphan());
//            Assert.assertEquals(savedItem.getUser(), savedItem.getCarouselItem().getUser());
            assertNotNull(staticInfoDao.removeCarouselItemFromSavedItem(savedItem.getId()));
            assertFalse("has no Carousel Item", savedItem.hasCarouselItem());
//            assertFalse("EuropeanaId has no associated CarouselItem", savedItem.getEuropeanaId().hasCarouselItem());
        }
        savedItems = user.getSavedItems();
        assertNotNull(savedItems);
    }

//    @Test
    public void testRemoveOrphanedCarouselItem() throws Exception {
        EuropeanaId europeanaId1 = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        EuropeanaId europeanaId2 = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_1);
        assertFalse("Orphan should be false", europeanaId1.isOrphan());
        assertTrue("Orphan should be true", europeanaId2.isOrphan());
        User user = userDao.addUser(user2);
        user = userDao.addSavedItem(user, new SavedItem(), EUROPEANA_URI_1);
        user = userDao.addSavedItem(user, new SavedItem(), EUROPEANA_URI_2);
        List<SavedItem> savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        assertEquals("list should have length 2", 2, savedItems.size());
        for (SavedItem savedItem : savedItems) {
            assertFalse("SavedItem should have no carousel item", savedItem.hasCarouselItem());
            assertNotNull(savedItem.getId());
            assertNotNull(staticInfoDao.createCarouselItem(savedItem.getId()));
            assertTrue("SavedItem should have one carousel item", savedItem.hasCarouselItem());
        }
        savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        assertEquals("list should be 1", 1, staticInfoDao.fetchCarouselItems().size());
    }

//    @Test
    public void testSavedSearchToSearchTerm() throws Exception {
        User user = userDao.addUser(user2);
        SavedSearch search1 = new SavedSearch();
        search1.setDateSaved(new Date());
        search1.setLanguage(Language.NL);
        search1.setQueryString("max");
        search1.setQuery("query=max&fq=TYPE:IMAGE");
        user = userDao.addSavedSearch(user, search1);
        List<SavedSearch> searchList = user.getSavedSearches();
        assertNotNull(searchList);
        for (SavedSearch savedSearch : searchList) {
            assertFalse("has no Search Term", savedSearch.hasSearchTerm());
            assertNotNull("id should not be null", savedSearch.getId());
            assertNotNull(staticInfoDao.addSearchTerm(savedSearch.getId()));
            assertTrue("has one Search Term", savedSearch.hasSearchTerm());
        }
        user = userDao.updateUser(user);
        searchList = user.getSavedSearches();
        assertNotNull(searchList);
        for (SavedSearch savedSearch : searchList) {
            assertTrue("has one Search Term", savedSearch.hasSearchTerm());
            assertEquals("Saved Item and SearchTerm should have the same query",
                    search1.getQueryString(),
                    savedSearch.getSearchTerm().getProposedSearchTerm());
//            Assert.assertEquals(savedSearch.getUser(), savedSearch.getSearchTerm().getUser());
            user = staticInfoDao.removeSearchTerm(savedSearch.getId());
            assertFalse("has no Search Term", savedSearch.hasSearchTerm());
        }
    }

    private User createUser(String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRegistrationDate(new Date());
        return user;
    }
}