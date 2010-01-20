package eu.europeana.query;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 8, 2010 11:14:43 AM
 */

public interface PresentationQuery {
    String getUserSubmittedQuery();
    String getQueryForPresentation();
    String getQueryToSave();
    String getTypeQuery();
}
