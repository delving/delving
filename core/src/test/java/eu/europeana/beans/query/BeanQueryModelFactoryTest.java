package eu.europeana.beans.query;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 13, 2010 5:15:54 PM
 */
public class BeanQueryModelFactoryTest {
    @Test
    @Ignore
    public void testCreateFromQueryParams() throws Exception {
    }

    @Test
    @Ignore
    public void testCreateFromUri() throws Exception {
    }

    @Test
    public void testFindSolrQueryType() throws Exception {
        BeanQueryModelFactory beanQueryModelFactory = new BeanQueryModelFactory();
        final BeanQueryModelFactory.QueryType simpleQueryType = beanQueryModelFactory.findSolrQueryType("very simple query");
        Assert.assertEquals("simple query",
                BeanQueryModelFactory.QueryType.SIMPLE_QUERY,
                simpleQueryType);
        final BeanQueryModelFactory.QueryType moreLikeThis = beanQueryModelFactory.findSolrQueryType("europeana_uri:\"http://www.europeana.eu/bla/bla\"");
        Assert.assertEquals("more like this query",
                BeanQueryModelFactory.QueryType.MORE_LIKE_THIS_QUERY,
                moreLikeThis);
        // implement this part
        final BeanQueryModelFactory.QueryType advancedQueryType = beanQueryModelFactory.findSolrQueryType("advanced and Query");
        Assert.assertEquals("advanced query",
                BeanQueryModelFactory.QueryType.ADVANCED_QUERY,
                advancedQueryType);


    }
}
