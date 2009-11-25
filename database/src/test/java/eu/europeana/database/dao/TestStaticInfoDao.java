package eu.europeana.database.dao;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.dao.fixture.DatabaseFixture;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.migration.DataMigration;

import org.junit.Assert;
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
    private DatabaseFixture databaseFixture;

    private List<Partner> partners;
    private int partnerCount = 100;

    @Before
    public void prepare() throws IOException {
        // DataMigration migration = new DataMigration();
        // migration.readTableFromResource(DataMigration.Table.STATIC_PAGE);
        partners = databaseFixture.createPartners("Nicola", partnerCount);
        log.info("Partner 10: " + partners.get(10).getName());

    }


    @Test
    public void getAllPartnerItems() throws Exception {

        String name;
        boolean found = false;
        List<Partner> allPartners = staticInfoDao.getAllPartnerItems();
        assertNotNull(allPartners);
        assertTrue(partners.size() >= partnerCount);
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
// todo: these methods must be tested
//    List<Contributor> getAllContributors();
//    List<Partner> fetchPartners();
//    List<Contributor> getAllContributorsByIdentifier();
//    Partner savePartner(Partner partner);
//    Contributor saveContributor(Contributor contributor);
//    boolean removePartner(Long partnerId);
//    boolean removeContributor(Long contributorId);
//    StaticPage fetchStaticPage(StaticPageType pageType, Language language);
//    StaticPage saveStaticPage(Long staticPageId, String content);
//    Boolean removeCarouselItem(Long id);
//    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
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
//    void setStaticPage(StaticPageType pageType, Language language, String content);
//    List<StaticPage> getAllStaticPages();
//    List<SearchTerm> getAllSearchTerms();


}