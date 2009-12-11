package eu.europeana.database.dao;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.LanguageDao;
import eu.europeana.database.dao.fixture.DatabaseFixture;
import eu.europeana.database.domain.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;

import java.io.IOException;
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


    private static List<Partner> partners;
    private static List<Contributor> contributors;
    private static List<User> users;
    private static List<SavedItem> savedItems;
    private static List<EuropeanaId> europeanaIds;
    private static List<CarouselItem> carouselItems;
    private int instanceCount = 11;

    @Before
    public void init() throws IOException {
        if (partners == null) {
            partners = databaseFixture.createPartners("Nicola", instanceCount);
            log.info("Partner 10: " + partners.get(10).getName());
        }

        if (contributors == null) {
            contributors = databaseFixture.createContributors("Nicola", instanceCount);
            log.info("Contributor 10: " + contributors.get(10).getOriginalName());
        }

        if (users == null) {
            users = databaseFixture.createUsers("Nicola", instanceCount);
            log.info("users 10: " + users.get(10).getFirstName());
        }

/*
        if (europeanaIds == null) {
            europeanaIds = databaseFixture.createEuropeanaIds("Nicola", instanceCount);
            log.info("europeanaId 10: " + europeanaIds.get(10).getEuropeanaUri());
        }


        if (carouselItems == null) {
            carouselItems = databaseFixture.createCarouselItems("Nicola", instanceCount, europeanaIds);
            log.info("carouselItems 10: " + carouselItems.get(10).getEuropeanaUri());
        }*/
        /* if (savedItems == null) {
            savedItems = databaseFixture.createSavedItems("Nicola", instanceCount, carouselItems, europeanaIds, users);
            log.info("savedItems 10: " + savedItems.get(10).getAuthor());
        }*/
    }


    @Test
    public void getAllPartnerItems() throws Exception {

        String name;
        boolean found;
        List<Partner> allPartners = staticInfoDao.getAllPartnerItems();
        assertNotNull(allPartners);
        assertTrue(partners.size() >= instanceCount);
        for (Partner partner : partners) {
            name = partner.getName();
            found = false;
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
        String name;
        boolean found;
        List<Contributor> allContributors = staticInfoDao.getAllContributors();
        assertNotNull(allContributors);
        assertTrue(contributors.size() >= instanceCount);
        for (Contributor contributor : contributors) {
            name = contributor.getOriginalName();
            found = false;
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
        String name;
        boolean found;
        List<Contributor> allContributors = staticInfoDao.getAllContributorsByIdentifier();
        assertNotNull(allContributors);
        assertTrue(contributors.size() >= instanceCount);
        for (Contributor contributor : contributors) {
            name = contributor.getOriginalName();
            found = false;
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
        Long partnerId = partners.get(10).getId();
        assertTrue(staticInfoDao.removePartner(partnerId));
        assertNull(databaseFixture.getPartner(partnerId));
    }


    @Test
    public void removeContributor() {
        Long contributorId = contributors.get(10).getId();
        assertTrue(staticInfoDao.removeContributor(contributorId));
        assertNull(databaseFixture.getContributor(contributorId));
    }

    @Test
    public void getStaticPage() {

        StaticPage staticPage;
        for (StaticPageType pageType : StaticPageType.values()) {
            for (Language language : languageDao.getActiveLanguages()) {
                staticPage = staticInfoDao.getStaticPage(pageType, language);
                assertNotNull(staticPage);
                assertEquals(staticPage.getPageType(), pageType);
                assertEquals(staticPage.getLanguage(), language);
                log.info("Testing PageType: " + pageType + "language: " + language);
            }
        }

    }

    @Test
    public void updateStaticPage() {

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

    //@Test
    public void createCarouselItem() {

        carouselItems.clear();
        log.info("Testing createCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(europeanaIds.get(walk), savedItems.get(walk).getId());
            assertNotNull(carouselItem);
            assertEquals(carouselItem.getEuropeanaId().getCreated(), europeanaIds.get(walk).getCreated());
            carouselItems.add(carouselItem);
        }
    }


    //@Test
    public void removeCarouselItem() {

        boolean removed;
        log.info("Testing removeCarouselItem: ");
        for (int walk = 0; walk < instanceCount; walk++) {
            removed = staticInfoDao.removeCarouselItem(carouselItems.get(walk).getId());
            assertTrue(removed);
        }
    }

// todo: these methods must be tested

//    void removeFromCarousel(SavedItem savedItem);
//    List<CarouselItem> fetchCarouselItems();
//    List<EditorPick> fetchEditorPicksItems();
//    void removeFromEditorPick(SavedSearch savedSearch);
//    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
//    User removeCarouselItem(User user, Long savedItemId);
//    User removeSearchTerm(User user, Long savedSearchId);
//    User addEditorPick(User user, EditorPick editorPick);
//    SearchTerm addSearchTerm(Long savedSearchId);
//    StaticPage fetchStaticPage (Language language, String pageName);
//    List<SearchTerm> getAllSearchTerms();


}