package eu.europeana.database;

import eu.europeana.database.domain.SearchTerm;

import java.util.List;

/**
 * @author vitali
 */
public interface SearchTermDao {
    List<SearchTerm> getAllSearchTerms();
}
