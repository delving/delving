package eu.europeana.database.dao;

import eu.europeana.database.StaticInfoDao;
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
        "/database-application-context.xml",
        "/hypersonic-datasource.xml"
})

public class TestStaticInfoDao {

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Before
    public void prepare() throws IOException {
    }

// todo: these methods must be tested
//    List<Partner> getAllPartnerItems();
//    List<Contributor> getAllContributorItems();
//    List<Partner> fetchPartners();
//    List<Contributor> fetchContributors();
//    Partner savePartner(Partner partner);
//    Contributor saveContributor(Contributor contributor);
//    boolean removePartner(Long partnerId);
//    boolean removeContributor(Long contributorId);
//    StaticPage fetchStaticPage(StaticPageType pageType, Language language);
//    StaticPage saveStaticPage(Long staticPageId, String content);
//    Boolean removeCarouselItem(Long id);
//    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
//    void removeFromCarousel(SavedItem savedItem);
//    boolean addCarouselItem(SavedItem savedItem);
//    List<CarouselItem> fetchCarouselItems();
//    List<EditorPick> fetchEditorPicksItems();
//    void removeFromEditorPick(SavedSearch savedSearch);
//    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
//    User removeCarouselItem(User user, Long savedItemId);
//    User removeSearchTerm(User user, Long savedSearchId);
//    User addCarouselItem(User user, SavedItem savedItem);
//    User addEditorPick(User user, EditorPick editorPick);
//    User addCarouselItem(User user, CarouselItem carouselItem);
//    CarouselItem addCarouselItem(User user, Long savedItem);
//    SearchTerm addSearchTerm(Long savedSearchId);
//    StaticPage fetchStaticPage (Language language, String pageName);
//    void setStaticPage(StaticPageType pageType, Language language, String content);
//    List<StaticPage> getAllStaticPages();
//    List<MessageKey> getAllTranslationMessages();
//    List<SearchTerm> getAllSearchTerms();

    @Test
    public void test1() {
        Assert.fail();
    }
}