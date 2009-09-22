package eu.europeana.json.solr;

import eu.europeana.controller.util.QueryConstraints;
import eu.europeana.query.FacetType;
import eu.europeana.query.QueryModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 20, 2009: 4:45:10 PM
 */
public class SolrQueryModelTest {

    @Test
    public void testBuildFilterString () {
        QueryModel.Constraints constraints = new QueryConstraints(new String[]{"YEAR:1701", "YEAR:1925", "TYPE:IMAGE"});
        SolrQueryModel queryModel = new SolrQueryModel();
        queryModel.setQueryConstraints(constraints);
        String output = queryModel.buildFilterString(FacetType.YEAR);
        Assert.assertEquals("Filter string is build incorrectly", "{!tag=yr}YEAR:(\"1701\" OR \"1925\")", output);
    }

}

