package eu.europeana.json.solr;

import eu.europeana.query.QueryExpression;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test some query formats to see how they are interpreted
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class TestQueries {

    @Test
    public void testSampleQueries() throws Exception {
        for (String [] sample : SAMPLE_QUERIES) {
            SolrQueryExpression expression = new SolrQueryExpression(sample[0]);
            Assert.assertEquals(sample[1], expression.getQueryString());
        }
    }

    private static final String [][] SAMPLE_QUERIES = {
            { "Tony Blair   and  \t prime minister", "Tony Blair AND prime minister"},
            { "not Tony Blair or prime minister", "NOT Tony Blair OR prime minister"}
    };

    @Test
    public void runCases() throws Exception {
        for (Sample sample : SAMPLE_EXPRESSIONS) {
            SolrQueryExpression queryExpression = new SolrQueryExpression(sample.getQuery());
            System.out.println(queryExpression.getType());
            System.out.println(queryExpression.getBackendQueryString());
            Assert.assertEquals(sample.getType(), queryExpression.getType());
        }
    }

    private static final Sample[] SAMPLE_EXPRESSIONS = new Sample[]{
            new Sample(QueryExpression.QueryType.SIMPLE_QUERY, "heritage"), // simple query
            new Sample(QueryExpression.QueryType.SIMPLE_QUERY, "heritage cultural deadline"), // simple query
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "cultural (heritage OR bla*) AND bla2"), // implicit conjuction
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "((title:culture AND heritage) OR nature)"), //
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "YEAR:1915"), // implicit conjuction
            new Sample(QueryExpression.QueryType.SIMPLE_QUERY, "cultural AND heritage"), // explicit conjunction
            new Sample(QueryExpression.QueryType.SIMPLE_QUERY, "\"cultural AND heritage\""), // phrase
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "title:\"cultural AND heritage\""), // field + phrase search
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "cultural OR heritage"), // disjunction
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "cultural NOT heritage"), //negation
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "cultural -heritage"), //negation  with - operator
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "(cultural OR heritage) AND europeana"), // grouping and conjunction
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "title:((cultural OR heritage) AND europeana)"), // field + grouping and conjunction
            new Sample(QueryExpression.QueryType.ADVANCED_QUERY, "title:((cultural OR heritage) AND europeana) AND date:1980"), // field + grouping and conjunction
            new Sample(QueryExpression.QueryType.MORE_LIKE_THIS_QUERY, "europeana_uri:\"http://europeana.siebinga.org/resolve/record/900/2603\"") // + operator to specify explicit must contain to make a positive hit
    };

    private static class Sample {
        private String query;
        private QueryExpression.QueryType queryType;

        private Sample(QueryExpression.QueryType queryType, String query) {
            this.queryType = queryType;
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public QueryExpression.QueryType getType() {
            return queryType;
        }
    }
}
