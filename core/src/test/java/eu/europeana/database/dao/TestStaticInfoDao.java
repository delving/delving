package eu.europeana.database.dao;

import eu.europeana.database.LanguageDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.*;
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
        "/database-application-context.xml",
        "/test-application-context.xml"
})

@Transactional
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


    private List<Partner> partners;
    private List<Contributor> contributors;
    private List<SavedItem> savedItems;
    private List<CarouselItem> carouselItems;
    private List<SavedSearch> savedSearches;
    private List<User> users;
    private int instanceCount = 11;
    private String name = "Nicola";

    @Before
    public void init() throws IOException {
        partners = databaseFixture.createPartners(name, instanceCount);
        log.info("Partner " + (instanceCount - 1) + "  " + partners.get(instanceCount - 1).getName());
        users = databaseFixture.createUsers(name, instanceCount);
        log.info("users " + (instanceCount - 1) + "  " + users.get(instanceCount - 1).getFirstName());
        List<EuropeanaId> europeanaIds = databaseFixture.createEuropeanaIds(name, instanceCount);
        log.info("europeanaId " + (instanceCount - 1) + "  " + europeanaIds.get(instanceCount - 1).getEuropeanaUri());
        savedItems = databaseFixture.createSavedItems(name, instanceCount, europeanaIds, users);
        log.info("savedItems " + (instanceCount - 1) + "  " + savedItems.get(instanceCount - 1).getAuthor());
        savedSearches = databaseFixture.createSavedSearch(name, instanceCount, users);
        log.info("savedSearch " + (instanceCount - 1) + "  " + savedSearches.get(instanceCount - 1).getQuery());
        contributors = databaseFixture.createContributors(name, instanceCount);
        log.info("Contributor " + (instanceCount - 1) + "  " + contributors.get(instanceCount - 1).getOriginalName());
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
        log.info("getAllPartnerItems Test is OK! ");
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
        log.info("getAllContributors Test is OK! ");
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
        log.info("getAllContributorsByIdentifier Test is OK! ");
    }

    @Test
    public void saveContributor() {
        log.info("Testing saveContributor: ");
        String name = "MofidiedName";
        Contributor contributor = contributors.get(instanceCount - 1);
        contributor.setOriginalName(name);
        Contributor modifiedContributor = staticInfoDao.saveContributor(contributor);
        assertNotNull(modifiedContributor);
        assertEquals(contributor.getId(), modifiedContributor.getId());
        assertEquals(contributor.getOriginalName(), modifiedContributor.getOriginalName());
        assertEquals(name, databaseFixture.getContributor(modifiedContributor.getId()).getOriginalName());
        log.info("saveContributor Test is OK! ");
    }

    @Test
    public void savePartner() {
        log.info("Testing savePartner: ");
        String name = "MofidiedName";
        Partner partner = partners.get(instanceCount - 1);
        partner.setName(name);
        Partner modifiedPartner = staticInfoDao.savePartner(partner);
        assertNotNull(modifiedPartner);
        assertEquals(partner.getId(), modifiedPartner.getId());
        assertEquals(partner.getName(), modifiedPartner.getName());
        assertEquals(name, databaseFixture.getPartner(modifiedPartner.getId()).getName());
        log.info("savePartner Test is OK! ");
    }

    @Test
    public void removePartner() {
        log.info("Testing removePartner: ");
        for (Partner partner : partners) {
            Long partnerId = partner.getId();
            assertTrue(staticInfoDao.removePartner(partnerId));
            assertNull(databaseFixture.getPartner(partnerId));
        }
        partners = null;
        log.info("removePartner Test is OK! ");
    }


    @Test
    public void removeContributor() {
        log.info("Testing removeContributor: ");
        for (Contributor contributor : contributors) {
            Long id = contributor.getId();
            assertTrue(staticInfoDao.removeContributor(id));
            assertNull(databaseFixture.getContributor(id));
        }
        contributors = null;
        log.info("removeContributor Test is OK! ");
    }

    @Test
    public void getStaticPage() {
        for (StaticPageType pageType : StaticPageType.values()) {
            for (Language language : languageDao.getActiveLanguages()) {
                StaticPage staticPage = staticInfoDao.getStaticPage(pageType, language);
                assertNotNull(staticPage);
                assertEquals(pageType, staticPage.getPageType());
                assertEquals(language, staticPage.getLanguage());
                //     log.info("Testing PageType: " + pageType + "language: " + language);
            }
        }
        log.info("getStaticPage Test is OK! ");
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
        assertEquals(newContent, staticPage.getContent());
        log.info("updateStaticPage Test is OK! ");
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
            assertEquals(newContent, staticPage.getContent().substring(0, newContent.length()));
            log.info("Static Page: " + staticPage.getPageType() + " " + staticPage.getContent());
        }
        log.info("setAndGetAllStaticPage Test is OK! ");
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