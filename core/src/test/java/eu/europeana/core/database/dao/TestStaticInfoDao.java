package eu.europeana.core.database.dao;

import eu.europeana.core.database.StaticInfoDao;
import eu.europeana.core.database.domain.*;
import eu.europeana.fixture.DatabaseFixture;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/core-application-context.xml",
        "/test-application-context.xml"
})

@Transactional
public class TestStaticInfoDao {
    private Logger log = Logger.getLogger(TestStaticInfoDao.class);
    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private DatabaseFixture databaseFixture;

    @PersistenceContext
    protected EntityManager entityManager;


    private List<SavedItem> savedItems;
    private List<CarouselItem> carouselItems;
    private List<SavedSearch> savedSearches;
    private List<User> users;
    private int instanceCount = 11;
    private String name = "Nicola";

    @Before
    public void init() throws IOException {
        users = databaseFixture.createUsers(name, instanceCount);
        log.info("users " + (instanceCount - 1) + "  " + users.get(instanceCount - 1).getFirstName());
        List<EuropeanaId> europeanaIds = databaseFixture.createEuropeanaIds(name, instanceCount);
        log.info("europeanaId " + (instanceCount - 1) + "  " + europeanaIds.get(instanceCount - 1).getEuropeanaUri());
        savedItems = databaseFixture.createSavedItems(name, instanceCount, europeanaIds, users);
        log.info("savedItems " + (instanceCount - 1) + "  " + savedItems.get(instanceCount - 1).getAuthor());
        savedSearches = databaseFixture.createSavedSearch(name, instanceCount, users);
        log.info("savedSearch " + (instanceCount - 1) + "  " + savedSearches.get(instanceCount - 1).getQuery());
    }

    @Test
    public void createCarouselItems() {
        carouselItems = new ArrayList<CarouselItem>();
        log.info("Testing createCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItems.get(walk).getId());
            assertNotNull(carouselItem);
            carouselItems.add(carouselItem);
        }
        assertEquals(instanceCount, carouselItems.size());
        log.info("createCarouselItem Test is OK! ");
    }


    @Test
    public void fetchCarouselItems() {
        createCarouselItems();
        log.info("Testing fetchCarouselItems: ");
        List<CarouselItem> carouselItems = staticInfoDao.fetchCarouselItems();
        assertEquals(instanceCount, carouselItems.size());
        for (int walk = 0; walk < instanceCount; walk++) {
            assertTrue(carouselItems.get(walk).getTitle().indexOf(name) > 0);
        }
        log.info("fetchCarouselItems Test is OK! ");
    }

    @Test
    public void removeCarouselItem() {
        createCarouselItems();
        log.info("Testing removeCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            boolean removed = staticInfoDao.removeCarouselItem(id);
            assertTrue(removed);
            assertNull(databaseFixture.getCarouselItem(id));
        }
        log.info("removeCarouselItem Test is OK! ");
    }


    @Test
    public void removeFromCarousel() {
        createCarouselItems();
        log.info("Testing removeFromCarousel: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            savedItems.get(walk).setCarouselItem(carouselItems.get(walk));
            staticInfoDao.removeFromCarousel(savedItems.get(walk));
            assertNull(databaseFixture.getCarouselItem(id));
        }
        log.info("removeFromCarousel Test is OK! ");
    }

    @Test
    public void removeCarouselItemUserSavedItem() {
        createCarouselItems();
        log.info("Testing removeCarouselItemUserSavedItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            SavedItem savedItem = carouselItems.get(walk).getSavedItem();
            User user = staticInfoDao.removeCarouselItemFromSavedItem(savedItem.getId());
            assertNotNull(user);
            assertEquals(savedItem.getUser().getLastName(), user.getLastName());
            assertNull(databaseFixture.getCarouselItem(id));
        }
        log.info("removeCarouselItemUserSavedItem Test is OK! ");
    }

    @Test
    public void addSearchTerms() {
        createCarouselItems();
        log.info("Testing addSearchTerm: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = savedSearches.get(walk).getId();
            SearchTerm searchTerm = staticInfoDao.addSearchTerm(id);
            assertNotNull(searchTerm);
            assertEquals(savedSearches.get(walk).getQuery(), searchTerm.getSavedSearch().getQuery());
        }
        log.info("addSearchTerm Test is OK! ");
    }

    @Test
    public void addSearchTermsForLanguage() {
        createCarouselItems();
        log.info("Testing addSearchTermsForLanguage: ");
        for (Language language : Language.values()) {
            boolean done = staticInfoDao.addSearchTerm(language, name + language.getCode());
            assertTrue(done);
        }
        List<SearchTerm> searchTerms = staticInfoDao.getAllSearchTerms();
        assertEquals(Language.values().length, searchTerms.size());
        log.info("addSearchTermsForLanguage Test is OK! ");
    }


    @Test
    public void getAllSearchTerms() {
        addSearchTerms();
        log.info("Testing getAllSearchTerms: ");
        List<SearchTerm> searchTerms = staticInfoDao.getAllSearchTerms();
        assertNotNull(searchTerms);
        assertEquals(instanceCount, searchTerms.size());
        for (SearchTerm searchTerm : searchTerms) {
            String query = searchTerm.getSavedSearch().getQuery();
            boolean found = false;
            for (SavedSearch savedSearch : savedSearches) {
                if (savedSearch.getQuery().equals(query)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail();
            }

        }
        log.info("getAllSearchTerms Test is OK! ");
    }

    @Test
    public void getAllSearchTermsForLanguage() {
        addSearchTermsForLanguage();
        log.info("Testing getAllSearchTermsForLanguage: ");
        for (Language language : Language.values()) {
            List<String> searchTerms = staticInfoDao.fetchSearchTerms(language);
            assertNotNull(searchTerms);
            assertEquals(1, searchTerms.size());
            assertEquals(name + language.getCode(), searchTerms.get(0));
        }
        log.info("getAllSearchTermsForLanguage Test is OK! ");
    }

    @Test
    public void removeSearchTermsForLanguage() {
        addSearchTermsForLanguage();
        log.info("Testing removeSearchTermsForLanguage: ");
        for (Language language : Language.values()) {
            boolean done = staticInfoDao.removeSearchTerm(language, name + language.getCode());
            assertTrue(done);
            assertEquals(0, databaseFixture.getSearchTerm(language, name + language.getCode()).size());
        }
        log.info("removeSearchTermsForLanguage Test is OK! ");
    }


    @Test
    public void removeSearchTerm() {
        addSearchTerms();
        log.info("Testing removeSearchTerm: ");
        List<SavedSearch> searches = databaseFixture.getAllSavedSearch();
        for (SavedSearch savedSearch : searches) {
            Long savedSearchId = savedSearch.getId();
            Long searchSearchId = savedSearch.getSearchTerm().getId();
            User user = staticInfoDao.removeSearchTerm(savedSearchId);
            assertNotNull(user);
            assertEquals(savedSearch.getUser().getLastName(), user.getLastName());
            assertNull(databaseFixture.getSearchTerm(searchSearchId));
        }
        savedSearches = databaseFixture.createSavedSearch(this.name, instanceCount, users);
        addSearchTerms();
        log.info("removeSearchTerm Test is OK! ");
    }

}