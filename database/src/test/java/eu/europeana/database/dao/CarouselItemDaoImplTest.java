/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.database.dao;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.User;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 3, 2009: 5:19:40 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/database-application-context.xml", "/test-application-context.xml"})
@Transactional
public class CarouselItemDaoImplTest {

    // europeanaUri from 92001_Ag_EU_TELtreasures.xml
    public static final String EUROPEANA_URI_1 = "http://www.europeana.eu/resolve/record/92001/79F2A36A85CE59D4343770F4A560EBDF5F207735"; // is orphan
    public static final String EUROPEANA_URI_2 = "http://www.europeana.eu/resolve/record/92001/C4613D0F3AB97C7A1CCD0A20268BBD79E5CAB51F";
    private User user1;
    private User user2;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private UserDao userDao;

    @Before
    public void init() {
        user1 = createUser("test1", "tester1", "test1@tester.com");
        user2 = createUser("test2", "tester2", "test2@tester.com");

    }

    @Test
    public void testCreateNewCarouselItem() throws IOException, SAXException, ParserConfigurationException {
        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_1);
        CarouselItem item = europeanaId.createCarouselItem();
        assertNotNull(item);
        assertNotNull(item.getThumbnail());
        assertNotNull(item.getTitle());
        assertNotNull(item.getProvider());
        assertNotNull(item.getLanguage());
        assertNotNull(item.getCreator());
        System.out.println(item.getTitle());
    }

    @Test
    public void testFindOrphanedSavedItem() throws Exception {
        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        assertFalse("Orphan should be false", europeanaId.isOrphan());
        User user = userDao.addUser(user1);
        user = userDao.addSavedItem(user, EUROPEANA_URI_2);
        List<SavedItem> savedItems = user.getSavedItems();
        assertTrue(!savedItems.isEmpty());
        europeanaId.setOrphan(true);
        for (SavedItem savedItem : savedItems) {
            assertTrue("Orphan should be true", savedItem.getEuropeanaId().isOrphan());
            assertFalse("Orphan should not have carouselItem", savedItem.hasCarouselItem());
        }

    }

    @Test
    public void testSavedItemToCarouselItem() throws Exception {
        EuropeanaId europeanaId = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        assertFalse("Orphan should be false", europeanaId.isOrphan());
        User user = userDao.addUser(user2);
        user = userDao.addSavedItem(user, EUROPEANA_URI_1);
        user = userDao.refreshUser(user);
        List<SavedItem> savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        for (SavedItem savedItem : savedItems) {
            assertFalse("SavedItem should have no carousel item", savedItem.hasCarouselItem());
            assertNotNull(savedItem.getId());
//            user = userDao.addCarouselItem(user, savedItem.getId());
            CarouselItem carouselItem = dashboardDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId());
//            Assert.assertTrue(carouselItem.getSavedItem() != null);
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
            assertNotNull(userDao.removeCarouselItem(user, savedItem.getId()));
            assertFalse("has no Carousel Item", savedItem.hasCarouselItem());
//            assertFalse("EuropeanaId has no associated CarouselItem", savedItem.getEuropeanaId().hasCarouselItem());
        }
        savedItems = user.getSavedItems();
        assertNotNull(savedItems);
    }

    @Test
    public void testRemoveOrphanedCarouselItem() throws Exception {
        EuropeanaId europeanaId1 = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_2);
        EuropeanaId europeanaId2 = dashboardDao.fetchEuropeanaId(EUROPEANA_URI_1);
        assertFalse("Orphan should be false", europeanaId1.isOrphan());
        assertTrue("Orphan should be true", europeanaId2.isOrphan());
        User user = userDao.addUser(user2);
        user = userDao.addSavedItem(user, EUROPEANA_URI_1);
        user = userDao.addSavedItem(user, EUROPEANA_URI_2);
        user = userDao.refreshUser(user);
        List<SavedItem> savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        assertEquals("list should have length 2", 2, savedItems.size());
        for (SavedItem savedItem : savedItems) {
            assertFalse("SavedItem should have no carousel item", savedItem.hasCarouselItem());
            assertNotNull(savedItem.getId());
            assertNotNull(dashboardDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId()));
            assertTrue("SavedItem should have one carousel item", savedItem.hasCarouselItem());
        }
        savedItems = user.getSavedItems();
        assertNotNull(savedItems);
        assertEquals("list should be 1", 1, dashboardDao.fetchCarouselItems().size());
    }

    @Test
    public void testSavedSearchToSearchTerm() throws Exception {
        User user = userDao.addUser(user2);
        SavedSearch search1 = new SavedSearch();
        search1.setDateSaved(new Date());
        search1.setLanguage(Language.NL);
        search1.setQueryString("max");
        search1.setQuery("query=max&fq=TYPE:IMAGE");
        user = userDao.addSavedSearch(user, search1);
        user = userDao.refreshUser(user);
        List<SavedSearch> searchList = user.getSavedSearches();
        assertNotNull(searchList);
        for (SavedSearch savedSearch : searchList) {
            assertFalse("has no Search Term", savedSearch.hasSearchTerm());
            assertNotNull("id should not be null", savedSearch.getId());
            assertNotNull(userDao.addSearchTerm(savedSearch.getId()));
            assertTrue("has one Search Term", savedSearch.hasSearchTerm());
        }
        user = userDao.refreshUser(user);
        searchList = user.getSavedSearches();
        assertNotNull(searchList);
        for (SavedSearch savedSearch : searchList) {
            assertTrue("has one Search Term", savedSearch.hasSearchTerm());
            assertEquals("Saved Item and SearchTerm should have the same query",
                    search1.getQueryString(),
                    savedSearch.getSearchTerm().getProposedSearchTerm());
//            Assert.assertEquals(savedSearch.getUser(), savedSearch.getSearchTerm().getUser());
            user = userDao.removeSearchTerm(user, savedSearch.getId());
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
