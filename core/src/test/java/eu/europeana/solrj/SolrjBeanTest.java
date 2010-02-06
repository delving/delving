package eu.europeana.solrj;

import eu.europeana.beans.BriefBean;
import eu.europeana.beans.IdBean;
import eu.europeana.bootstrap.SolrStarter;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 6, 2010 9:02:06 AM
 */

public class SolrjBeanTest {

    private Logger log = Logger.getLogger(getClass());

    private static final String url = "http://localhost:8983/solr";
    /*
      CommonsHttpSolrServer is thread-safe and if you are using the following constructor,
      you *MUST* re-use the same instance for all requests.  If instances are created on
      the fly, it can cause a connection leak. The recommended practice is to keep a
      static instance of CommonsHttpSolrServer per solr server url and share it for all requests.
      See https://issues.apache.org/jira/browse/SOLR-861 for more details
    */

    private static CommonsHttpSolrServer server;

    @BeforeClass
    public static void init() throws Exception {
        // start the solr server
        SolrStarter solrStarter = new SolrStarter();
        solrStarter.start();
        server = new CommonsHttpSolrServer(url);
        server.setSoTimeout(1000);  // socket read timeout
        server.setConnectionTimeout(100);
        server.setDefaultMaxConnectionsPerHost(100);
        server.setMaxTotalConnections(100);
        server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
//        server.setAllowCompression(true);
        server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
    }


    @Test
    public void testSolrjGetBeans() throws Exception {
        SolrQuery query = new SolrQuery().setQuery("*:*");
        query.setFacet(true);
        query.addFacetField("PROVIDER");
        query.setQueryType("moreLikeThis");
        QueryResponse response = server.query(query);
        List<BriefBean> beans = response.getBeans(BriefBean.class);
        assertEquals(10, beans.size());
        for (BriefBean bean : beans) {
            log.info(String.format("bean: %s", bean.getId()));
        }

        List<FacetField> facetFieldList = response.getFacetFields();
        for (FacetField facetField : facetFieldList) {
            if (facetField.getName().equalsIgnoreCase("PROVIDER")) {
                List<FacetField.Count> list = facetField.getValues();
                for (FacetField.Count count : list) {
                    log.info(String.format("tag: %s%d", count.getName(), count.getCount()));
                }
            }
        }
    }

    @Test
    public void testSolrjgetBeanId() throws Exception {
        SolrQuery query = new SolrQuery().setQuery("*:*");
        query.setRows(10);
        query.setFields("europeana_uri", "timestamp");
        QueryResponse response = server.query(query);
        List<IdBean> list = response.getBeans(IdBean.class);
        Assert.assertNotNull(list);
        for (IdBean idBean : list) {
            log.info(String.format("bean: %s timestamp: %s", idBean.getEuropeanaUri(), idBean.getTimestamp()));
        }
    }
}