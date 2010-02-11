package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import eu.europeana.dashboard.client.dto.CarouselItemX;
import eu.europeana.dashboard.client.dto.DashboardLogX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;
import eu.europeana.dashboard.client.dto.LanguageX;
import eu.europeana.dashboard.client.dto.QueueEntryX;
import eu.europeana.dashboard.client.dto.SavedItemX;
import eu.europeana.dashboard.client.dto.SavedSearchX;
import eu.europeana.dashboard.client.dto.UserX;

import java.util.List;

public interface DashboardServiceAsync {

    void login(String email, String password, AsyncCallback<UserX> async);

    void fetchUsers(String pattern, AsyncCallback<List<UserX>> async);

    void updateUser(UserX user, AsyncCallback<UserX> async);

    void fetchSavedItems(Long userId, AsyncCallback<List<SavedItemX>> async);

    void removeUser(UserX user, AsyncCallback<Void> async);

    void fetchCollections(AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollections(String prefix, AsyncCallback<List<EuropeanaCollectionX>> async);

    void fetchCollection(String collectionName, String fileName, boolean create, AsyncCallback<EuropeanaCollectionX> async);

    void updateCollection(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchQueueEntries(AsyncCallback<List<QueueEntryX>> async);

    void updateCollectionCounters(EuropeanaCollectionX collection, AsyncCallback<EuropeanaCollectionX> async);

    void fetchImportFiles(boolean normalized, AsyncCallback<List<ImportFileX>> async);

    void commenceValidate(ImportFileX file, Long collectionId, AsyncCallback<ImportFileX> async);

    void commenceImport(ImportFileX file, Long collectionId, boolean normalized, AsyncCallback<ImportFileX> async);

    void abortImport(ImportFileX file, boolean normalized, AsyncCallback<ImportFileX> async);

    void checkImportFileStatus(String fileName, boolean normalized, AsyncCallback<ImportFileX> async);

    void fetchLanguages(AsyncCallback<List<LanguageX>> async);

    void fetchCacheUrl(AsyncCallback<String> async);

    void fetchCarouselItems(AsyncCallback<List<CarouselItemX>> async);

    void createCarouselItem(SavedItemX savedItemX, AsyncCallback<CarouselItemX> async);

    void removeCarouselItem(CarouselItemX item, AsyncCallback<Boolean> async);

    void addSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void fetchSearchTerms(String language, AsyncCallback<List<String>> async);

    void removeSearchTerm(String language, String term, AsyncCallback<Boolean> async);

    void fetchSavedSearches(UserX userX, AsyncCallback<List<SavedSearchX>> async);

    void fetchLogEntriesFrom(Long topId, int pageSize, AsyncCallback<List<DashboardLogX>> async);

    void fetchLogEntriesTo(Long bottomId, int pageSize, AsyncCallback<List<DashboardLogX>> async);

    void disableAllCollections(AsyncCallback<Void> async);

    void enableAllCollections(AsyncCallback<Void> async);
}
