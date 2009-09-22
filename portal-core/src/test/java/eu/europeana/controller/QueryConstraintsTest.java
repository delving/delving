package eu.europeana.controller;

import eu.europeana.controller.util.QueryConstraints;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test the NextQueryLink building
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QueryConstraintsTest {

    @Test
    public void performRequest() throws Exception {
        String [] constraintStrings = new String[] {
                "YEAR:1900",
                "YEAR:1901",
                "LOCATION:Here",
        };
        QueryConstraints qc = new QueryConstraints(constraintStrings);
        Assert.assertEquals(qc.toQueryString(), "&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here");
        List<QueryConstraints.Breadcrumb> crumbs = qc.toBreadcrumbs("query","kultur");
        String [][] expect = new String[][] {
                {"query=kultur", "kultur", "false" },
                {"query=kultur&qf=YEAR:1900", "YEAR:1900", "false" },
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901", "YEAR:1901", "false"},
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here", "LOCATION:Here", "true" },
        };
        int index = 0;
        for (QueryConstraints.Breadcrumb crumb : crumbs) {
            Assert.assertEquals(crumb.getHref(),expect[index][0]);
            Assert.assertEquals(crumb.getDisplay(),expect[index][1]);
            Assert.assertEquals(String.valueOf(crumb.getLast()),expect[index][2]);
            index++;
        }
    }
}