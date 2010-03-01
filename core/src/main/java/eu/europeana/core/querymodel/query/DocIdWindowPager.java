package eu.europeana.core.querymodel.query;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 9, 2010 12:47:37 PM
 */
public interface DocIdWindowPager {
    DocIdWindow getDocIdWindow();

    boolean isNext();

    boolean isPrevious();

    String getQueryStringForPaging();

    String getFullDocUri();

    String getNextUri();

    int getNextInt();

    String getPreviousUri();

    int getPreviousInt();

    String getQuery();

    String getReturnToResults();

    String getPageId();

    String getTab();

    String toString();

    String getStartPage();
}
