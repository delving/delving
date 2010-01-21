package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;
import eu.europeana.dashboard.client.dto.*;

import java.util.List;
import java.util.Set;

/**
 * This is the client side of the RPC connection needed by the dashboard
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardService extends RemoteService {

    UserX login(String email, String password);
    List<UserX> fetchUsers(String pattern);
    UserX updateUser(UserX user);
    List<SavedItemX> fetchSavedItems(Long userId);
    void removeUser(UserX user);

    List<EuropeanaCollectionX> fetchCollections();
    List<EuropeanaCollectionX> fetchCollections(String prefix);
    EuropeanaCollectionX fetchCollection(String name, boolean create);
    EuropeanaCollectionX updateCollection(EuropeanaCollectionX collection);
    List<QueueEntryX> fetchQueueEntries();
    EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection);

    List<ImportFileX> fetchImportFiles(boolean normalized);
    ImportFileX commenceValidate(ImportFileX file, Long collectionId);
    ImportFileX commenceImport(ImportFileX file, Long collectionId, boolean normalized);
    ImportFileX abortImport(ImportFileX file, boolean normalized);
    ImportFileX checkImportFileStatus(String fileName, boolean normalized);

    List<String> fetchMessageKeys();
    List<LanguageX> fetchLanguages();
    List<TranslationX> fetchTranslations(String key, Set<String> languageCodes);
    TranslationX setTranslation(String key, String language, String value);

    String fetchCacheUrl();
    List<CarouselItemX> fetchCarouselItems();
    CarouselItemX createCarouselItem(SavedItemX savedItemX);
    boolean removeCarouselItem(CarouselItemX item);

    boolean addSearchTerm(String language, String term);
    List<String> fetchSearchTerms(String language);
    boolean removeSearchTerm(String language, String term);

    List<String> getObjectOrphans();
    boolean deleteObjectOrphan(String uri);
    void deleteAllOrphans();

    List<SavedSearchX> fetchSavedSearches(UserX userX);

    List<String> fetchPartnerSectors();
    List<PartnerX> fetchPartners();
    List<CountryX> fetchCountries();
    List<ContributorX> fetchContributors();
    PartnerX savePartner(PartnerX partner);
    ContributorX saveContributor(ContributorX contributor);

    boolean removePartner(Long partnerId);
    boolean removeContributor(Long contributorId);

    List<String> fetchStaticPageTypes();
    StaticPageX fetchStaticPage(String pageType, LanguageX language);
    StaticPageX saveStaticPage(Long staticPageId, String content);

    void removeMessageKey(String key);
    void addMessageKey(String key);

    List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize);
    List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize);

    void disableAllCollections ();
    void enableAllCollections ();
}
