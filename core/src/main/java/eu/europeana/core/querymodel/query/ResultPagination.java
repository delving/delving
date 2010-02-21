package eu.europeana.core.querymodel.query;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 8, 2010 10:51:42 AM
 */
public interface ResultPagination {
    boolean isPrevious();

    boolean isNext();

    int getPreviousPage();

    int getNextPage();

    int getLastViewableRecord();

    int getNumFound();

    int getRows();

    int getStart();

    List<PageLink> getPageLinks();

    List<Breadcrumb> getBreadcrumbs();

    PresentationQuery getPresentationQuery();

    int getPageNumber();
}
