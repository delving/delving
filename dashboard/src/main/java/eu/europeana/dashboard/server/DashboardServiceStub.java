package eu.europeana.dashboard.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import eu.europeana.dashboard.client.DashboardService;
import eu.europeana.dashboard.client.dto.*;

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

    @Override
    public List<EuropeanaCollectionX> fetchCollections() {
        return service().fetchCollections();
    }

    @Override
    public List<EuropeanaCollectionX> fetchCollections(String prefix) {
        return service().fetchCollections(prefix);
    }

    @Override
    public EuropeanaCollectionX fetchCollection(String collectionName, String fileName, boolean create) {
        return service().fetchCollection(collectionName, fileName, create);
    }

    @Override
    public EuropeanaCollectionX updateCollection(EuropeanaCollectionX collection) {
        return service().updateCollection(collection);
    }

    @Override
    public List<QueueEntryX> fetchQueueEntries() {
        return service().fetchQueueEntries();
    }

    @Override
    public EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection) {
        return service().updateCollectionCounters(collection);
    }

    @Override
    public UserX login(String email, String password) {
        return service().login(email, password);
    }

    @Override
    public List<UserX> fetchUsers(String pattern) {
        return service().fetchUsers(pattern);
    }

    @Override
    public UserX updateUser(UserX user) {
        return service().updateUser(user);
    }

    @Override
    public List<SavedItemX> fetchSavedItems(Long userId) {
        return service().fetchSavedItems(userId);
    }

    @Override
    public void removeUser(UserX user) {
        service().removeUser(user);
    }

    @Override
    public ImportFileX commenceValidate(ImportFileX file, Long collectionId) {
        return service().commenceValidate(file, collectionId);
    }

    @Override
    public List<ImportFileX> fetchImportFiles(boolean normalized) {
        return service().fetchImportFiles(normalized);
    }

    @Override
    public ImportFileX commenceImport(ImportFileX file, Long collectionId, boolean normalized) {
        return service().commenceImport(file, collectionId, normalized);
    }

    @Override
    public ImportFileX abortImport(ImportFileX file, boolean normalized) {
        return service().abortImport(file, normalized);
    }

    @Override
    public ImportFileX checkImportFileStatus(String fileName, boolean normalized) {
        return service().checkImportFileStatus(fileName, normalized);
    }

    @Override
    public List<LanguageX> fetchLanguages() {
        return service().fetchLanguages();
    }

    @Override
    public String fetchCacheUrl() {
        return service().fetchCacheUrl();
    }

    @Override
    public List<CarouselItemX> fetchCarouselItems() {
        return service().fetchCarouselItems();
    }

    @Override
    public CarouselItemX createCarouselItem(SavedItemX savedItemX) {
        return service().createCarouselItem(savedItemX);
    }

    @Override
    public boolean removeCarouselItem(CarouselItemX item) {
        return service().removeCarouselItem(item);
    }

    @Override
    public boolean addSearchTerm(String language, String term) {
        return service().addSearchTerm(language, term);
    }

    @Override
    public List<String> fetchSearchTerms(String language) {
        return service().fetchSearchTerms(language);
    }

    @Override
    public boolean removeSearchTerm(String language, String term) {
        return service().removeSearchTerm(language, term);
    }

    @Override
    public List<SavedSearchX> fetchSavedSearches(UserX user) {
        return service().fetchSavedSearches(user);
    }

    @Override
    public List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize) {
        return service().fetchLogEntriesFrom(topId, pageSize);
    }

    @Override
    public List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize) {
        return service().fetchLogEntriesTo(bottomId, pageSize);
    }

    @Override
    public void disableAllCollections() {
        service().disableAllCollections();
    }

    @Override
    public void enableAllCollections() {
        service().enableAllCollections();
    }

    private DashboardService service() {
        return HostedModeServiceLoader.getDashboardService();
    }
}