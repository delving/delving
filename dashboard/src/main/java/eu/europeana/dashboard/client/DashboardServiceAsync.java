package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import eu.europeana.dashboard.client.dto.CarouselItemX;
import eu.europeana.dashboard.client.dto.ContributorX;
import eu.europeana.dashboard.client.dto.CountryX;
import eu.europeana.dashboard.client.dto.DashboardLogX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.EuropeanaIdX;
import eu.europeana.dashboard.client.dto.ImportFile;
import eu.europeana.dashboard.client.dto.LanguageX;
import eu.europeana.dashboard.client.dto.PartnerX;
import eu.europeana.dashboard.client.dto.QueueEntryX;
import eu.europeana.dashboard.client.dto.RoleX;
import eu.europeana.dashboard.client.dto.SavedItemX;
import eu.europeana.dashboard.client.dto.SavedSearchX;
import eu.europeana.dashboard.client.dto.StaticPageX;
import eu.europeana.dashboard.client.dto.TranslationX;
import eu.europeana.dashboard.client.dto.UserX;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DashboardServiceAsync {

    void login(String email, String password, AsyncCallback<UserX> async);

    void setUserRole(Long userId, RoleX role, AsyncCallback<Void> async);

    void fetchUsers(String pattern, AsyncCallback<List<UserX>> async);

    void setUserEnabled(Long userId, boolean enabled, AsyncCallback<Void> async);

    void setUserLanguages(Long userId, String languages, AsyncCallback<Void> async);

    void setUserProjectId(Long userId, String projectId, AsyncCallback<Void> async);

    void setUserProviderId(Long userId, String providerId, AsyncCallback<Void> async);

    void fetchSavedItems(Long userId, AsyncCallback<List<SavedItemX>> async);

    void removeUser(Long userId, AsyncCallback<Void> async);

    void fetchCollections(AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollections(String prefix, AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollection(String name, boolean create, AsyncCallback<EuropeanaCollectionX> async);

    void updateCollection(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchQueueEntries(AsyncCallback<List<QueueEntryX>> async);

    void updateCollectionCounters(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchImportFiles(boolean normalized, AsyncCallback<List<ImportFile>> async);

    void commenceValidate(ImportFile file, Long collectionId, AsyncCallback<ImportFile> async);

    void commenceImport(ImportFile file, Long collectionId, boolean normalized, AsyncCallback<ImportFile> async);

    void abortImport(ImportFile file, boolean normalized, AsyncCallback<ImportFile> async);

    void checkImportFileStatus(String fileName, boolean normalized, AsyncCallback<ImportFile> async);

    void fetchMessageKeys(AsyncCallback<List<String>> async);

    void fetchLanguages(AsyncCallback<List<LanguageX>> async);

    void fetchTranslations(Set<String> languageCodes, AsyncCallback<Map<String, List<TranslationX>>> async);

    void setTranslation(String key, String language, String value, AsyncCallback<TranslationX> async);

    void fetchCacheUrl(AsyncCallback<String> async);

    void fetchCarouselItems(AsyncCallback<List<CarouselItemX>> async);

    void createCarouselItem(String europeanaUri, AsyncCallback<CarouselItemX> async);

    void removeCarouselItem(CarouselItemX item, AsyncCallback<Boolean> async);

    void addSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void fetchSearchTerms(String language, AsyncCallback<List<String>> async);

    void removeSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void getObjectOrphans(AsyncCallback<List<String>> async);

    void deleteObjectOrphan(String uri, AsyncCallback<Boolean> async);

    void deleteAllOrphans(AsyncCallback<Void> async);

    void fetchEuropeanaId(String uri, AsyncCallback<EuropeanaIdX> async);

    void updateEuropeanaId(EuropeanaIdX europeanaId, AsyncCallback<EuropeanaIdX> async);

    void fetchSavedSearches(Long id, AsyncCallback<List<SavedSearchX>> async);

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
