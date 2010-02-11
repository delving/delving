package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;
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
    EuropeanaCollectionX fetchCollection(String collectionName, String fileName, boolean create);
    EuropeanaCollectionX updateCollection(EuropeanaCollectionX collection);
    List<QueueEntryX> fetchQueueEntries();
    EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection);

    List<ImportFileX> fetchImportFiles(boolean normalized);
    ImportFileX commenceValidate(ImportFileX file, Long collectionId);
    ImportFileX commenceImport(ImportFileX file, Long collectionId, boolean normalized);
    ImportFileX abortImport(ImportFileX file, boolean normalized);
    ImportFileX checkImportFileStatus(String fileName, boolean normalized);

    List<LanguageX> fetchLanguages();

    String fetchCacheUrl();
    List<CarouselItemX> fetchCarouselItems();
    CarouselItemX createCarouselItem(SavedItemX savedItemX);
    boolean removeCarouselItem(CarouselItemX item);

    boolean addSearchTerm(String language, String term);
    List<String> fetchSearchTerms(String language);
    boolean removeSearchTerm(String language, String term);

    List<SavedSearchX> fetchSavedSearches(UserX userX);

    List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize);
    List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize);

    void disableAllCollections ();
    void enableAllCollections ();
}
