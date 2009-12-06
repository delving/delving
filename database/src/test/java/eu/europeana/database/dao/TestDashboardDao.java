package eu.europeana.database.dao;

import eu.europeana.database.DashboardDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.*;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * @author todo insert: "name" <email>
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {
//        "/database-application-context.xml"
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
    public void testCreateNewCarouselItem() throws IOException, SAXException, ParserConfigurationException {
//        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_1);
        // todo rewrite this to test create carouselItem from saved item
        CarouselItem item = new SavedItem().createCarouselItem();
        assertNotNull(item);
        assertNotNull(item.getThumbnail());
        assertNotNull(item.getTitle());
        assertNotNull(item.getProvider());
        assertNotNull(item.getLanguage());
        assertNotNull(item.getCreator());
        System.out.println(item.getTitle());
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
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId());
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
            assertNotNull(staticInfoDao.removeCarouselItem(user, savedItem.getId()));
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
            assertNotNull(staticInfoDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId()));
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
            user = staticInfoDao.removeSearchTerm(user, savedSearch.getId());
            assertFalse("has no Search Term", savedSearch.hasSearchTerm());
        }
    }

//    @Test
    public void testFindOrphans() {
        EuropeanaCollection testCol = dashboardDao.fetchCollectionByName("TestCollection", true);
        Set<String> objectUrls = new TreeSet<String>();
        // create 10 orphans
        createIds(testCol, objectUrls, 0, false);
        // update the last modified date and update collection
        testCol.setCollectionLastModified(new Date());
        dashboardDao.updateCollection(testCol);
        // create 10 records normal records
        createIds(testCol, objectUrls, 10, false);

        EuropeanaCollection updatedCollection = dashboardDao.updateCollectionCounters(testCol.getId());
        Assert.assertEquals("total number objects", 0, updatedCollection.getTotalObjects().intValue());
        Assert.assertEquals("total number records", 10, updatedCollection.getTotalRecords().intValue());
        Assert.assertEquals("total number of orphans", 10, updatedCollection.getTotalOrphans().intValue());

        int orphans = dashboardDao.markOrphans(testCol);
        Assert.assertEquals("total number of orphans", 10, orphans);

        boolean addedToQueue = dashboardDao.addToIndexQueue(testCol);
        Assert.assertTrue("check if collection is added to the indexing queue", addedToQueue);
        IndexingQueueEntry indexingQueueEntry = dashboardDao.getEntryForIndexing();
        Assert.assertEquals("Check if we are indexing the test collection", testCol.getId(), indexingQueueEntry.getCollection().getId());

        List<EuropeanaId> idsForIndexing = dashboardDao.getEuropeanaIdsForIndexing(100, indexingQueueEntry);
        Assert.assertEquals("See if number of ids to index does not include orphans", 10, idsForIndexing.size());
    }


//    @Test
    public void testWithObjects() {
        long start = System.currentTimeMillis();
        EuropeanaCollection collection = dashboardDao.fetchCollectionByName("TestCollection", true);
        runWithObjects(collection);
        log.info("finished first run in: " + (System.currentTimeMillis() - start));
        EuropeanaId iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(0, collection).getEuropeanaUri());
        Assert.assertEquals(2, iddy.getEuropeanaObjects().size());

        start = System.currentTimeMillis();
        runWithObjects(collection);
        log.info("finished second run in: " + (System.currentTimeMillis() - start));
//        start = System.currentTimeMillis();
//        runWithObjects(collection);
//        log.info("finished third run in: " + (System.currentTimeMillis() - start));
        iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertEquals(2, iddy.getEuropeanaObjects().size());
        for (EuropeanaObject europeanaObject : iddy.getEuropeanaObjects()) {
            log.info(String.format("object: %s", europeanaObject.getObjectUrl()));
        }
        Assert.assertEquals(iddy.getEuropeanaUri(), createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertNotSame(iddy.getEuropeanaUri(), createEuropeanaId(0, collection).getEuropeanaUri());
    }

//    @Test
    public void testWithoutObjects() {
        long start = System.currentTimeMillis();
        EuropeanaCollection collection = dashboardDao.fetchCollectionByName("TestCollection", true);
        Set<String> objectUrls = new TreeSet<String>();
        runWithoutObjects(collection, objectUrls);
        log.info("finished first run in: " + (System.currentTimeMillis() - start));
        EuropeanaId iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(0, collection).getEuropeanaUri());
        Assert.assertEquals(0, iddy.getEuropeanaObjects().size());

        start = System.currentTimeMillis();
        runWithoutObjects(collection, objectUrls);
        log.info("finished second run in: " + (System.currentTimeMillis() - start));
        iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertEquals(0, iddy.getEuropeanaObjects().size());
        Assert.assertEquals(iddy.getEuropeanaUri(), createEuropeanaId(99, collection).getEuropeanaUri());
    }

    private void runWithObjects(EuropeanaCollection collection) {
        for (int walk = 0; walk < 100; walk++) {
            EuropeanaId id = createEuropeanaId(walk, collection);
            Set<String> objectList = createObjectList(walk);
            dashboardDao.saveEuropeanaId(id, objectList);
//            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectList.size()));
        }
    }

    private void runWithoutObjects(EuropeanaCollection collection, Set<String> objectUrls) {
        for (int walk = 0; walk < 100; walk++) {
            EuropeanaId id = createEuropeanaId(walk, collection);
            dashboardDao.saveEuropeanaId(id, objectUrls);
//            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectUrls.size()));
        }
    }

    private void createIds(EuropeanaCollection collection, Set<String> objectUrls, int offSet, Boolean createOrphan) {
        for (int walk = 0; walk < 10; walk++) {
            EuropeanaId id = createEuropeanaId(walk + offSet, collection);
            if (createOrphan) {
                id.setOrphan(true);
            }
            dashboardDao.saveEuropeanaId(id, objectUrls);
            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectUrls.size()));
        }
    }


    private Set<String> createObjectList(int walk) {
        Set<String> objectUrls = new TreeSet<String>();
        for (int i = 0; i < 2; i++) {
            String object = new StringBuilder().append("http://host/objecturl/pathy/thing").append(walk).append(i).append(".jpg").toString();
            objectUrls.add(object);
        }
        return objectUrls;
    }

    private EuropeanaId createEuropeanaId(int number, EuropeanaCollection collection) {
        EuropeanaId europeanaId = new EuropeanaId(collection);
        europeanaId.setCreated(new Date());
        europeanaId.setLastModified(new Date());
        europeanaId.setEuropeanaUri("http://www.europeana.eu/resolve/record/92001/79F2A36A85CE59D4343770F4A560EBDF5F207735" + number);
        return europeanaId;
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