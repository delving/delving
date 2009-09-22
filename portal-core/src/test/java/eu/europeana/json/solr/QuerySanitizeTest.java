package eu.europeana.json.solr;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the query expression thing
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QuerySanitizeTest {
    private static final String [][] SAMPLE_EXPRESSIONS = {
            { "Tony Blair   and  \t prime minister", "Tony Blair AND prime minister"},
            { "not Tony Blair or prime minister", "NOT Tony Blair OR prime minister"}
    };

    @Test
    public void runCases() throws Exception {
        for (String [] sample : SAMPLE_EXPRESSIONS) {
            SolrQueryExpression expression = new SolrQueryExpression(sample[0]);
            Assert.assertEquals(sample[1], expression.getQueryString());
        }
    }
}