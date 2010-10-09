package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

import java.util.List;
import java.util.Map;

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

    String getNextFullDocUrl();

    String getPreviousFullDocUrl();

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

    List<Breadcrumb> getBreadcrumbs();

    int getFullDocUriInt();

    void setPortalName(String portalName);

    void initialize(Map<String, String[]> httpParameters, SolrQuery originalBriefSolrQuery, SolrServer solrServer, Class<? extends DocId> idBean) throws SolrServerException, EuropeanaQueryException;

    String getSortBy();
}
