package eu.europeana.database;

import eu.europeana.database.domain.SearchTerm;

import java.util.List;

/**
 * @author vitali
 */

// todo: remove this DAO, use this function in StaticInfoDao

public interface SearchTermDao {

    List<SearchTerm> getAllSearchTerms();

}
