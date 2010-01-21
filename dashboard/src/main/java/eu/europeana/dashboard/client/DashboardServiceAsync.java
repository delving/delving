package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import eu.europeana.dashboard.client.dto.*;

import java.util.List;
import java.util.Set;

public interface DashboardServiceAsync {

    void login(String email, String password, AsyncCallback<UserX> async);

    void fetchUsers(String pattern, AsyncCallback<List<UserX>> async);

    void updateUser(UserX user, AsyncCallback<UserX> async);

    void fetchSavedItems(Long userId, AsyncCallback<List<SavedItemX>> async);

    void removeUser(UserX user, AsyncCallback<Void> async);

    void fetchCollections(AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollections(String prefix, AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollection(String name, boolean create, AsyncCallback<EuropeanaCollectionX> async);

    void updateCollection(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchQueueEntries(AsyncCallback<List<QueueEntryX>> async);

    void updateCollectionCounters(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchImportFiles(boolean normalized, AsyncCallback<List<ImportFileX>> async);

    void commenceValidate(ImportFileX file, Long collectionId, AsyncCallback<ImportFileX> async);

    void commenceImport(ImportFileX file, Long collectionId, boolean normalized, AsyncCallback<ImportFileX> async);

    void abortImport(ImportFileX file, boolean normalized, AsyncCallback<ImportFileX> async);

    void checkImportFileStatus(String fileName, boolean normalized, AsyncCallback<ImportFileX> async);

    void fetchMessageKeys(AsyncCallback<List<String>> async);

    void fetchLanguages(AsyncCallback<List<LanguageX>> async);

    void fetchTranslations(String key, Set<String> languageCodes, AsyncCallback<List<TranslationX>> async);

    void setTranslation(String key, String language, String value, AsyncCallback<TranslationX> async);

    void fetchCacheUrl(AsyncCallback<String> async);

    void fetchCarouselItems(AsyncCallback<List<CarouselItemX>> async);

    void createCarouselItem(SavedItemX savedItemX, AsyncCallback<CarouselItemX> async);

    void removeCarouselItem(CarouselItemX item, AsyncCallback<Boolean> async);

    void addSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void fetchSearchTerms(String language, AsyncCallback<List<String>> async);

    void removeSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void getObjectOrphans(AsyncCallback<List<String>> async);

    void deleteObjectOrphan(String uri, AsyncCallback<Boolean> async);

    void deleteAllOrphans(AsyncCallback<Void> async);

    void fetchSavedSearches(UserX userX, AsyncCallback<List<SavedSearchX>> async);

    void fetchPartnerSectors(AsyncCallback<List<String>> async);

    void fetchPartners(AsyncCallback<List<PartnerX>> async);

    void fetchCountries(AsyncCallback<List<CountryX>> async);

    void fetchContributors(AsyncCallback<List<ContributorX>> async);

    void savePartner(PartnerX partner, AsyncCallback<PartnerX> async);

    void saveContributor(ContributorX contributor, AsyncCallback<ContributorX> async);

    void removePartner(Long partnerId, AsyncCallback<Boolean> async);

    void removeContributor(Long contributorId, AsyncCallback<Boolean> async);

    void fetchStaticPageTypes(AsyncCallback<List<String>> async);

    void fetchStaticPage(String pageType, LanguageX language, AsyncCallback<StaticPageX> async);

    void saveStaticPage(Long staticPageId, String content, AsyncCallback<StaticPageX> async);

    void removeMessageKey(String key, AsyncCallback<Void> async);

    void addMessageKey(String key, AsyncCallback<Void> async);

    void fetchLogEntriesFrom(Long topId, int pageSize, AsyncCallback<List<DashboardLogX>> async);

    void fetchLogEntriesTo(Long bottomId, int pageSize, AsyncCallback<List<DashboardLogX>> async);

    void disableAllCollections(AsyncCallback<Void> async);

    void enableAllCollections(AsyncCallback<Void> async);
}
