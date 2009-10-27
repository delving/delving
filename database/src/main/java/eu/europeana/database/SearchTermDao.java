package eu.europeana.database;

import eu.europeana.database.domain.SearchTerm;

import java.util.List;

/**
 * @author vitali
 */
public interface SearchTermDao {
    
    List<SearchTerm> getAllSearchTerms();

    // todo: add these (implementations in DashboardDaoImpl)
//    boolean addSearchTerm(Language language, String term);
//    boolean addSearchTerm(SavedSearch savedSearch);
//    List<String> fetchSearchTerms(Language language);
//    boolean removeSearchTerm(Language language, String term);
}
