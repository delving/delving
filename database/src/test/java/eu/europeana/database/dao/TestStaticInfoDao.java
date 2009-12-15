package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.dao.fixture.DatabaseFixture;
import eu.europeana.database.domain.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml",
        "/test-application-context.xml"
})

public class TestStaticInfoDao {
    private Logger log = Logger.getLogger(TestStaticInfoDao.class);
    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private LanguageDao languageDao;


    @Autowired
    private DatabaseFixture databaseFixture;

    @PersistenceContext
    protected EntityManager entityManager;


    private static List<Partner> partners;
    private static List<Contributor> contributors;
    private static List<SavedItem> savedItems;
    private static List<CarouselItem> carouselItems;
    private static List<SavedSearch> savedSearches;
    private int instanceCount = 11;
    private String name = "Nicola";

    @Before
    public void init() throws IOException {
        if (partners == null) {
            partners = databaseFixture.createPartners(name, instanceCount);
            log.info("Partner 10: " + partners.get(10).getName());
            contributors = databaseFixture.createContributors(name, instanceCount);
            log.info("Contributor 10: " + contributors.get(10).getOriginalName());
            List<User> users = databaseFixture.createUsers(name, instanceCount);
            log.info("users 10: " + users.get(10).getFirstName());
            List<EuropeanaId> europeanaIds = databaseFixture.createEuropeanaIds(name, instanceCount);
            log.info("europeanaId 10: " + europeanaIds.get(10).getEuropeanaUri());
            carouselItems = new ArrayList<CarouselItem>();
            savedItems = databaseFixture.createSavedItems(name, instanceCount, europeanaIds, users);
            log.info("savedItems 10: " + savedItems.get(10).getAuthor());
            savedSearches = databaseFixture.createSavedSearch(name, instanceCount, users);
            log.info("savedSearch 10: " + savedSearches.get(10).getQuery());
        }
    }


    @Test
    public void getAllPartnerItems() throws Exception {
        log.info("Testing getAllPartnerItems: ");
        List<Partner> allPartners = staticInfoDao.getAllPartnerItems();
        assertNotNull(allPartners);
        assertTrue(partners.size() >= instanceCount);
        for (Partner partner : partners) {
            String name = partner.getName();
            boolean found = false;
            for (Partner dbPartner : allPartners) {
                if (dbPartner.getName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail();
            }
        }
    }

    @Test
    public void getAllContributors() {
        log.info("Testing getAllContributors: ");
        List<Contributor> allContributors = staticInfoDao.getAllContributors();
        assertNotNull(allContributors);
        assertTrue(contributors.size() >= instanceCount);
        for (Contributor contributor : contributors) {
            String name = contributor.getOriginalName();
            boolean found = false;
            for (Contributor dbContributor : allContributors) {
                if (dbContributor.getOriginalName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail();
            }
        }
    }

    @Test
    public void getAllContributorsByIdentifier() {
        log.info("Testing getAllContributorsByIdentifier: ");
        List<Contributor> allContributors = staticInfoDao.getAllContributorsByIdentifier();
        assertNotNull(allContributors);
        assertTrue(contributors.size() >= instanceCount);
        for (Contributor contributor : contributors) {
            String name = contributor.getOriginalName();
            boolean found = false;
            for (Contributor dbContributor : allContributors) {
                if (dbContributor.getOriginalName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail();
            }
        }
    }

    @Test
    public void saveContributor() {
        log.info("Testing saveContributor: ");
        String name = "MofidiedName";
        Contributor contributor = contributors.get(10);
        contributor.setOriginalName(name);
        Contributor modifiedContributor = staticInfoDao.saveContributor(contributor);
        assertNotNull(modifiedContributor);
        assertEquals(contributor.getId(), modifiedContributor.getId());
        assertEquals(contributor.getOriginalName(), modifiedContributor.getOriginalName());
        assertEquals(name, databaseFixture.getContributor(modifiedContributor.getId()).getOriginalName());
    }

    @Test
    public void savePartner() {
        log.info("Testing savePartner: ");
        String name = "MofidiedName";
        Partner partner = partners.get(10);
        partner.setName(name);
        Partner modifiedPartner = staticInfoDao.savePartner(partner);
        assertNotNull(modifiedPartner);
        assertEquals(partner.getId(), modifiedPartner.getId());
        assertEquals(partner.getName(), modifiedPartner.getName());
        assertEquals(name, databaseFixture.getPartner(modifiedPartner.getId()).getName());
    }

    @Test
    public void removePartner() {
        log.info("Testing removePartner: ");
        Long partnerId = partners.get(10).getId();
        assertTrue(staticInfoDao.removePartner(partnerId));
        assertNull(databaseFixture.getPartner(partnerId));
    }


    @Test
    public void removeContributor() {
        log.info("Testing removeContributor: ");
        Long contributorId = contributors.get(10).getId();
        assertTrue(staticInfoDao.removeContributor(contributorId));
        assertNull(databaseFixture.getContributor(contributorId));
    }

    @Test
    public void getStaticPage() {
        for (StaticPageType pageType : StaticPageType.values()) {
            for (Language language : languageDao.getActiveLanguages()) {
                StaticPage staticPage = staticInfoDao.getStaticPage(pageType, language);
                assertNotNull(staticPage);
                assertEquals(staticPage.getPageType(), pageType);
                assertEquals(staticPage.getLanguage(), language);
                log.info("Testing PageType: " + pageType + "language: " + language);
            }
        }

    }

    @Test
    public void updateStaticPage() {
        log.info("Testing updateStaticPage: ");
        StaticPage staticPage;
        String newContent = "This is the new page content";
        staticPage = staticInfoDao.getStaticPage(StaticPageType.ABOUT_US, Language.EN);
        assertNotNull(staticPage);
        staticPage = staticInfoDao.updateStaticPage(staticPage.getId(), newContent);
        assertNotNull(staticPage);
        staticPage = staticInfoDao.getStaticPage(StaticPageType.ABOUT_US, Language.EN);
        assertEquals(staticPage.getContent(), newContent);
    }

    @Test
    public void setAndGetAllStaticPage() {
        log.info("Testing setAndGetAllStaticPage: ");
        int pageCount = 0;
        String newContent = "content for ";
        for (StaticPageType pageType : StaticPageType.values()) {
            for (Language language : languageDao.getActiveLanguages()) {
                staticInfoDao.setStaticPage(pageType, language, newContent + language.getName());
                pageCount++;
            }
        }

        List<StaticPage> staticPages = staticInfoDao.getAllStaticPages();
        assertEquals(pageCount, staticPages.size());
        for (StaticPage staticPage : staticPages) {
            assertEquals(staticPage.getContent().substring(0, newContent.length()), newContent);
            log.info("Static Page: " + staticPage.getPageType() + " " + staticPage.getContent());
        }
    }

    @Test
    public void createCarouselItem() {
        carouselItems.clear();
        log.info("Testing createCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItems.get(walk).getId());
            assertNotNull(carouselItem);
            carouselItems.add(carouselItem);
        }
        assertEquals(carouselItems.size(), instanceCount);
    }


    @Test
    public void fetchCarouselItems() {
        log.info("Testing fetchCarouselItems: ");
        List<CarouselItem> carouselItems = staticInfoDao.fetchCarouselItems();
        assertEquals(carouselItems.size(), instanceCount);
        for (int walk = 0; walk < instanceCount; walk++) {
            assertTrue(carouselItems.get(walk).getTitle().indexOf(name) > 0);
        }
    }

    @Test
    public void removeCarouselItem() {
        log.info("Testing removeCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            boolean removed = staticInfoDao.removeCarouselItem(id);
            assertTrue(removed);
            assertNull(databaseFixture.getCarouselItem(id));
        }
    }


    @Test
    public void removeFromCarousel() {
        log.info("Testing removeFromCarousel: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            staticInfoDao.removeFromCarousel(savedItems.get(walk));
            assertNull(databaseFixture.getCarouselItem(id));
        }
    }


    @Test
    public void removeCarouselItemUserSavedItem() {
        log.info("Testing removeCarouselItemUserSavedItem: ");
        createCarouselItem();
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = carouselItems.get(walk).getId();
            SavedItem savedItem = carouselItems.get(walk).getSavedItem();
            User user = staticInfoDao.removeCarouselItem(savedItem.getUser(), savedItem.getId());
            assertNotNull(user);
            assertEquals(user.getLastName(), savedItem.getUser().getLastName());
            assertNull(databaseFixture.getCarouselItem(id));
        }
    }


    @Test
    public void addSearchTerm() {
        log.info("Testing addSearchTerm: ");
        createCarouselItem();
        for (int walk = 0; walk < instanceCount; walk++) {
            Long id = savedSearches.get(walk).getId();
            SearchTerm searchTerm = staticInfoDao.addSearchTerm(id);
            assertNotNull(searchTerm);
            assertEquals(searchTerm.getSavedSearch().getQuery(), savedSearches.get(walk).getQuery());
        }
    }


    @Test
    public void getAllSearchTerms() {
        log.info("Testing getAllSearchTerms: ");
        List<SearchTerm> searchTerms = staticInfoDao.getAllSearchTerms();
        assertNotNull(searchTerms);
        assertTrue(searchTerms.size() == instanceCount);
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
    }


    @Test
    public void removeSearchTerm() {
        log.info("Testing removeSearchTerm: ");
        List<SavedSearch> savedSearches = databaseFixture.getAllSavedSearch();
        for (SavedSearch savedSearch : savedSearches) {
            Long savedSearchId = savedSearch.getId();
            Long searchSearchId = savedSearch.getSearchTerm().getId();
            User user = savedSearch.getUser();
            user = staticInfoDao.removeSearchTerm(user, savedSearchId);
            assertNotNull(user);
            assertEquals(user.getLastName(), savedSearch.getUser().getLastName());
            assertNull(databaseFixture.getSearchTerm(searchSearchId));
        }
    }

}