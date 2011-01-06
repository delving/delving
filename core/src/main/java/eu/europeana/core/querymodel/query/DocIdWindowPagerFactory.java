package eu.europeana.core.querymodel.query;

import eu.delving.metadata.MetadataModel;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since May 10, 2010 11:51:57 PM
 */
public class DocIdWindowPagerFactory {

    @Value("#{launchProperties['portal.name']}")
    private String portalName = "portal"; // must be injected later


    public DocIdWindowPager getPager(Map<String, String[]> params, SolrQuery solrQuery, SolrServer solrServer, MetadataModel metadataModel) {
        DocIdWindowPager pager = new DocIdWindowPagerImpl();
        pager.setPortalName(portalName);
        try {
            pager.initialize(params, solrQuery, solrServer, metadataModel);
        } catch (SolrServerException e) {
            return null; // when there are no results from solr return null
        } catch (EuropeanaQueryException e) {
            return null; // when there are no results from solr return null
        }
        return pager;
    }
}
