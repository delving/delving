package eu.europeana.solrj;

import eu.europeana.core.querymodel.query.QueryType;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test that the blob is properly (un)marshalling.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestSolrQueryBlob {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void testSolrjQuery() throws Exception {
        SolrQuery before = new SolrQuery()
                .setQueryType(QueryType.SIMPLE_QUERY.toString())
                .setRows(10)
                .setFacet(true)
                .setFilterQueries()
                .setQuery("*:*");

        SolrQueryBlob blob = new SolrQueryBlob(before);
        SolrQuery after = blob.getQuery();
        log.info("query:\n"+after.toString());
        log.info("blob:\n"+blob.toString());
        assertEquals(before.getQueryType(), after.getQueryType());
        assertEquals(before.getFields(), after.getFields());
    }
}