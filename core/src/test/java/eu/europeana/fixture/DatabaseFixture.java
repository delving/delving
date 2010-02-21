package eu.europeana.fixture;

import eu.europeana.core.database.domain.*;
import eu.europeana.core.querymodel.query.DocType;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tools to put some data into the database for testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

public class DatabaseFixture {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public List<User> createUsers(String who, int count) {
        List<User> users = new ArrayList<User>();
        for (int walk = 0; walk < count; walk++) {
            User user = new User(
                    null,
                    "user-" + who + walk,
                    who + walk + "@email.com",
                    "password-" + who + walk,
                    "First Name " + who + walk,
                    "Last Name " + who + walk,
                    "", "", "", false,
                    Role.ROLE_USER, true
            );
            entityManager.persist(user);
            users.add(user);
        }
        entityManager.flush();
        return users;
    }

    @Transactional
    public List<EuropeanaId> createEuropeanaIds(String collectionName, int count) {
        EuropeanaCollection collection = new EuropeanaCollection();
        collection.setName(collectionName);
        collection.setDescription("Created for testing");
        entityManager.persist(collection);
        List<EuropeanaId> ids = new ArrayList<EuropeanaId>();
        for (int walk = 0; walk < count; walk++) {
            EuropeanaId id = new EuropeanaId(collection);
            id.setCreated(new Date());
            id.setEuropeanaUri("http://europeana.uri.pretend/item" + walk);
            id.setBoostFactor(Float.valueOf("0.5"));
            id.setTimesViewed(0);
            id.setCarouselItems(null);
            id.setLastModified(new Date());
            id.setLastViewed(new Date());
            id.setOrphan(false);
            id.setSocialTags(null);
            entityManager.persist(id);
            ids.add(id);
        }
        return ids;
    }

    @Transactional
    public EuropeanaId fetchEuropeanaId(Long id) {
        EuropeanaId europeanaId = entityManager.find(EuropeanaId.class, id);
        europeanaId.getSocialTags().size();
        return europeanaId;
    }

    @Transactional
    public List<SavedItem> createSavedItems(String name, int count, List<EuropeanaId> europeanaIds, List<User> users) {
        List<SavedItem> savedItems = new ArrayList<SavedItem>();
        for (int walk = 0; walk < count; walk++) {
            User user = entityManager.find(User.class, users.get(walk).getId());
            SavedItem savedItem = new SavedItem();
            savedItem.setAuthor("Author " + name + walk);
            savedItem.setDateSaved(new Date());
            savedItem.setDocType(DocType.IMAGE);
            savedItem.setEuropeanaId(europeanaIds.get(walk));
            savedItem.setEuropeanaObject("http://europeana.uri.pretend/item" + walk);
            savedItem.setTitle("Title " + name + walk);
            savedItem.setUser(user);
            savedItem.setLanguage(Language.EN);
            user.getSavedItems().add(savedItem);
            entityManager.persist(savedItem);
            savedItems.add(savedItem);
        }
        return savedItems;
    }

    @Transactional
    public List<CarouselItem> createCarouselItems(String name, int count, List<EuropeanaId> europeanaIds) {
        List<CarouselItem> carouselItems = new ArrayList<CarouselItem>();

        for (int walk = 0; walk < count; walk++) {
            CarouselItem carouselItem = new CarouselItem();
            carouselItem.setCreator("Creator " + name + walk);
            carouselItem.setEuropeanaUri("http://europeana.uri.pretend/item" + walk);
            carouselItem.setEuropeanaId(europeanaIds.get(walk));
            carouselItem.setLanguage(Language.EN);
            carouselItem.setProvider("Provider " + name + walk);
            carouselItem.setTitle("Title " + name + walk);
            carouselItem.setThumbnail("thumbnail " + name + walk);
            carouselItem.setYear("2009");
            carouselItem.setType(DocType.IMAGE);
            entityManager.persist(carouselItem);
            carouselItems.add(carouselItem);
        }
        return carouselItems;
    }

    @Transactional
    public List<SavedSearch> createSavedSearch(String name, int count, List<User> users) {

        List<SavedSearch> savedSearchs = new ArrayList<SavedSearch>();
        Language languages[] = Language.values();

        for (int walk = 0; walk < count; walk++) {
            SavedSearch savedSearch = new SavedSearch();
            savedSearch.setDateSaved(new Date());
            savedSearch.setLanguage(languages[walk]);
            savedSearch.setQuery(name + " query" + walk);
            savedSearch.setQueryString(name + " querystring" + walk);
            savedSearch.setUser(users.get(walk));
            savedSearchs.add(savedSearch);
        }
        for (int walk = 0; walk < count; walk++) {
            User user = entityManager.find(User.class, users.get(walk).getId());
            user.setSavedSearches(savedSearchs);
            entityManager.persist(savedSearchs.get(walk));
        }

        return savedSearchs;
    }

    @Transactional
    public CarouselItem getCarouselItem(Long id) {
        return entityManager.find(CarouselItem.class, id);
    }


    @Transactional
    public SearchTerm getSearchTerm(Long id) {
        return entityManager.find(SearchTerm.class, id);
    }


    @Transactional
    public List<String> getSearchTerm(Language language, String term) {
        Query query = entityManager.createQuery("select s from SearchTerm s where s.language = :language and s.proposedSearchTerm = :term");
        query.setParameter("term", term);
        query.setParameter("language", language);
        return (List<String>) query.getResultList();
    }

    @Transactional
    public SavedSearch getSavedSearch(Long id) {
        return entityManager.find(SavedSearch.class, id);
    }

    @Transactional
    public List<SavedSearch> getAllSavedSearch() {
        Query query = entityManager.createQuery("select s from SavedSearch s order by s.id ");
        return (List<SavedSearch>) query.getResultList();

    }


}
