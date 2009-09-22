package eu.europeana.dashboard.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import eu.europeana.dashboard.client.DashboardService;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public EuropeanaCollectionX fetchCollection(String name, boolean create) {
        return service().fetchCollection(name, create);
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

    public void setUserRole(Long userId, RoleX role) {
        service().setUserRole(userId, role);
    }

    public List<UserX> fetchUsers(String pattern) {
        return service().fetchUsers(pattern);
    }

    public void setUserEnabled(Long userId, boolean enabled) {
        service().setUserEnabled(userId, enabled);
    }

    public void setUserLanguages(Long userId, String languages) {
        service().setUserLanguages(userId, languages);
    }

    public void setUserProjectId(Long userId, String projectId) {
        service().setUserProjectId(userId, projectId);
    }

    public void setUserProviderId(Long userId, String providerId) {
        service().setUserProviderId(userId, providerId);
    }

    public List<SavedItemX> fetchSavedItems(Long userId) {
        return service().fetchSavedItems(userId);
    }

    public void removeUser(Long userId) {
        service().removeUser(userId);
    }

    public boolean deleteImportFile(ImportFile file, boolean normalized) {
        return service().deleteImportFile(file, normalized);
    }

    public ImportFile commenceValidate(ImportFile file, Long collectionId) {
        return service().commenceValidate(file, collectionId);
    }

    public List<ImportFile> fetchImportFiles(boolean normalized) {
        return service().fetchImportFiles(normalized);
    }

    public ImportFile commenceImport(ImportFile file, Long collectionId, boolean normalized) {
        return service().commenceImport(file, collectionId, normalized);
    }

    public ImportFile abortImport(ImportFile file, boolean normalized) {
        return service().abortImport(file, normalized);
    }

    public ImportFile checkImportFileStatus(String fileName, boolean normalized) {
        return service().checkImportFileStatus(fileName, normalized);
    }

    public List<String> fetchMessageKeys() {
        return service().fetchMessageKeys();
    }

    public List<LanguageX> fetchLanguages() {
        return service().fetchLanguages();
    }

    public Map<String, List<TranslationX>> fetchTranslations(Set<String> keys) {
        return service().fetchTranslations(keys);
    }

    public TranslationX setTranslation(String key, String language, String value) {
        return service().setTranslation(key, language, value);
    }

    public String fetchCacheUrl() {
        return service().fetchCacheUrl();
    }

    public List<CarouselItemX> fetchCarouselItems() {
        return service().fetchCarouselItems();
    }

    public CarouselItemX createCarouselItem(String europeanaUri) {
        return service().createCarouselItem(europeanaUri);
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

    public List<String> getObjectOrphans() {
        return service().getObjectOrphans();
    }

    public boolean deleteObjectOrphan(String uri) {
        return service().deleteObjectOrphan(uri);
    }

    public void deleteAllOrphans() {
        service().deleteAllOrphans();
    }

    public EuropeanaIdX fetchEuropeanaId(String uri) {
        return service().fetchEuropeanaId(uri);
    }

    public EuropeanaIdX updateEuropeanaId(EuropeanaIdX europeanaId) {
        return service().updateEuropeanaId(europeanaId);
    }

    public List<SavedSearchX> fetchSavedSearches(Long id) {
        return service().fetchSavedSearches(id);
    }

    public List<String> fetchPartnerSectors() {
        return service().fetchPartnerSectors();
    }

    public List<PartnerX> fetchPartners() {
        return service().fetchPartners();
    }

    public List<CountryX> fetchCountries() {
        return service().fetchCountries();
    }

    public List<ContributorX> fetchContributors() {
        return service().fetchContributors();
    }

    public PartnerX savePartner(PartnerX partner) {
        return service().savePartner(partner);
    }

    public ContributorX saveContributor(ContributorX contributor) {
        return service().saveContributor(contributor);
    }

    public boolean removePartner(Long partnerId) {
        return service().removePartner(partnerId);
    }

    public boolean removeContributor(Long contributorId) {
        return service().removeContributor(contributorId);
    }

    public List<String> fetchStaticPageTypes() {
        return service().fetchStaticPageTypes();
    }

    public StaticPageX fetchStaticPage(String pageType, LanguageX language) {
        return service().fetchStaticPage(pageType, language);
    }

    public StaticPageX saveStaticPage(Long staticPageId, String content) {
        return service().saveStaticPage(staticPageId, content);
    }

    public void removeMessageKey(String key) {
        service().removeMessageKey(key);
    }

    public void addMessageKey(String key) {
        service().addMessageKey(key);
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