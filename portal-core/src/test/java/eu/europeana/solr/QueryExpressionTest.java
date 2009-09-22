package eu.europeana.solr;

import eu.europeana.json.solr.SolrQueryExpression;
import eu.europeana.query.QueryExpression;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the query expression thing
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QueryExpressionTest {
    private static final Sample[] SAMPLE_EXPRESSIONS = new Sample[]{
            new Sample(QueryExpression.Type.SIMPLE_QUERY, "heritage"), // simple query
            new Sample(QueryExpression.Type.SIMPLE_QUERY, "heritage cultural deadline"), // simple query
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "cultural (heritage OR bla*) AND bla2"), // implicit conjuction
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "((title:culture AND heritage) OR nature)"), //
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "YEAR:1915"), // implicit conjuction
            new Sample(QueryExpression.Type.SIMPLE_QUERY, "cultural AND heritage"), // explicit conjunction
            new Sample(QueryExpression.Type.SIMPLE_QUERY, "\"cultural AND heritage\""), // phrase
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "title:\"cultural AND heritage\""), // field + phrase search
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "cultural OR heritage"), // disjunction
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "cultural NOT heritage"), //negation
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "cultural -heritage"), //negation  with - operator
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "(cultural OR heritage) AND europeana"), // grouping and conjunction
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "title:((cultural OR heritage) AND europeana)"), // field + grouping and conjunction
            new Sample(QueryExpression.Type.ADVANCED_QUERY, "title:((cultural OR heritage) AND europeana) AND date:1980"), // field + grouping and conjunction
            new Sample(QueryExpression.Type.MORE_LIKE_THIS_QUERY, "europeana_uri:\"http://europeana.siebinga.org/resolve/record/900/2603\"") // + operator to specify explicit must contain to make a positive hit
    };

    @Test
    public void runCases() throws Exception {
        for (Sample sample : SAMPLE_EXPRESSIONS) {
            SolrQueryExpression queryExpression = new SolrQueryExpression(sample.getQuery());
            System.out.println(queryExpression.getType());
            System.out.println(queryExpression.getBackendQueryString());
            Assert.assertEquals(sample.getType(), queryExpression.getType());
//            Assert.assertTrue(sample.getQuery()+" has advanced="+queryExpression.isAdvanced(), queryExpression.isAdvanced() == sample.isAdvanced());
//            Assert.assertTrue(sample.getQuery()+" has isMoreLikeThis="+queryExpression.isMoreLikeThis(), queryExpression.isMoreLikeThis() == sample.isMoreLikeThis());
        }
    }

    private static class Sample {
        private String query;
        private QueryExpression.Type type;

        private Sample(QueryExpression.Type type, String query) {
            this.type = type;
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public QueryExpression.Type getType() {
            return type;
        }
    }
}