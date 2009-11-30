package eu.europeana.database.dao;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.dao.fixture.DatabaseFixture;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.Contributor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;

import javax.persistence.NoResultException;
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
    private List<Contributor> contributors;
    private int instanceCount = 11;

    @Before
    public void prepare() throws IOException {
        // DataMigration migration = new DataMigration();
        // migration.readTableFromResource(DataMigration.Table.STATIC_PAGE);
        partners = databaseFixture.createPartners("Nicola", instanceCount);
        log.info("Partner 10: " + partners.get(10).getName());
        contributors = databaseFixture.createContributors("Nicola", instanceCount);
        log.info("Contributor 10: " + contributors.get(10).getOriginalName());

    }

    /*

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
          */
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
    /*
  @Test
  public void savePartner() {
      String name  =  "MofidiedName";
      Partner partner = partners.get(10);
      partner.setName(name);
      Partner modifiedPartner = staticInfoDao.savePartner(partner);
      assertNotNull(modifiedPartner);
      assertEquals(partner.getId(), modifiedPartner.getId());
      assertEquals(partner.getName(), modifiedPartner.getName());
      assertEquals(name, databaseFixture.getPartner(modifiedPartner.getId()).getName());
  }
      /*
  @Test
  public void removePartner() {
      Long partnerId = partners.get(10).getId();
      assertTrue(staticInfoDao.removePartner(partnerId));
      assertNull(staticInfoDao.getPartner(partnerId));
  }
    */
    /*
@Test
public void removeContributor() {
Long contributorId = contributors.get(10).getId();
assertTrue(staticInfoDao.removeContributor(contributorId));
assertNull(staticInfoDao.getContributor(contributorId));
}            */

    // todo: tests works fine if executed one at a time, fail if you run 2 or more tests.
// todo: these methods must be tested
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