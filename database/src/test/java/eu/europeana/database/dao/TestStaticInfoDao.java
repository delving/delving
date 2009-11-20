package eu.europeana.database.dao;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.migration.DataMigration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @author todo insert: "name" <email>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml"
})

public class TestStaticInfoDao {

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Before
    public void prepare() throws IOException {
        DataMigration migration = new DataMigration();
        migration.readTableFromResource(DataMigration.Table.STATIC_PAGE);
    }

// todo: these methods must be tested
//    List<Partner> getAllPartnerItems();
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

    @Test
    public void test1() {
        Assert.fail();
    }
}