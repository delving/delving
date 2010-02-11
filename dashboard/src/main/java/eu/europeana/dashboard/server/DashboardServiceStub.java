package eu.europeana.dashboard.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import eu.europeana.dashboard.client.DashboardService;
import eu.europeana.dashboard.client.dto.CarouselItemX;
import eu.europeana.dashboard.client.dto.DashboardLogX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;
import eu.europeana.dashboard.client.dto.LanguageX;
import eu.europeana.dashboard.client.dto.QueueEntryX;
import eu.europeana.dashboard.client.dto.SavedItemX;
import eu.europeana.dashboard.client.dto.SavedSearchX;
import eu.europeana.dashboard.client.dto.UserX;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DashboardServiceStub extends RemoteServiceServlet implements DashboardService {
    private static final long serialVersionUID = 4889955602075719736L;

    @Override
    protected void service(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserCookie.set(request.getParameter("cookie"));
        super.service(request, response);
    }

    public List<EuropeanaCollectionX> fetchCollections() {
        return service().fetchCollections();
    }

    public List<EuropeanaCollectionX> fetchCollections(String prefix) {
        return service().fetchCollections(prefix);
    }

    public EuropeanaCollectionX fetchCollection(String collectionName, String fileName, boolean create) {
        return service().fetchCollection(collectionName, fileName, create);
    }

    public EuropeanaCollectionX updateCollection(EuropeanaCollectionX collection) {
        return service().updateCollection(collection);
    }

    public List<QueueEntryX> fetchQueueEntries() {
        return service().fetchQueueEntries();
    }

    public EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection) {
        return service().updateCollectionCounters(collection);
    }

    public UserX login(String email, String password) {
        return service().login(email, password);
    }

    public List<UserX> fetchUsers(String pattern) {
        return service().fetchUsers(pattern);
    }

    public UserX updateUser(UserX user) {
        return service().updateUser(user);
    }

    public List<SavedItemX> fetchSavedItems(Long userId) {
        return service().fetchSavedItems(userId);
    }

    public void removeUser(UserX user) {
        service().removeUser(user);
    }

    public ImportFileX commenceValidate(ImportFileX file, Long collectionId) {
        return service().commenceValidate(file, collectionId);
    }

    public List<ImportFileX> fetchImportFiles(boolean normalized) {
        return service().fetchImportFiles(normalized);
    }

    public ImportFileX commenceImport(ImportFileX file, Long collectionId, boolean normalized) {
        return service().commenceImport(file, collectionId, normalized);
    }

    public ImportFileX abortImport(ImportFileX file, boolean normalized) {
        return service().abortImport(file, normalized);
    }

    public ImportFileX checkImportFileStatus(String fileName, boolean normalized) {
        return service().checkImportFileStatus(fileName, normalized);
    }

    public List<LanguageX> fetchLanguages() {
        return service().fetchLanguages();
    }

    public String fetchCacheUrl() {
        return service().fetchCacheUrl();
    }

    public List<CarouselItemX> fetchCarouselItems() {
        return service().fetchCarouselItems();
    }

    public CarouselItemX createCarouselItem(SavedItemX savedItemX) {
        return service().createCarouselItem(savedItemX);
    }

    public boolean removeCarouselItem(CarouselItemX item) {
        return service().removeCarouselItem(item);
    }

    public boolean addSearchTerm(String language, String term) {
        return service().addSearchTerm(language, term);
    }

    public List<String> fetchSearchTerms(String language) {
        return service().fetchSearchTerms(language);
    }

    public boolean removeSearchTerm(String language, String term) {
        return service().removeSearchTerm(language, term);
    }

    public List<SavedSearchX> fetchSavedSearches(UserX user) {
        return service().fetchSavedSearches(user);
    }

    public List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize) {
        return service().fetchLogEntriesFrom(topId, pageSize);
    }

    public List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize) {
        return service().fetchLogEntriesTo(bottomId, pageSize);
    }

    public void disableAllCollections() {
        service().disableAllCollections();
    }

    public void enableAllCollections() {
        service().enableAllCollections();
    }

    private DashboardService service() {
        return HostedModeServiceLoader.getDashboardService();
    }
}