package eu.europeana.json.sql;

import eu.europeana.query.QueryExpression;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 4, 2009: 9:24:36 AM
 */
public class SqlQueryExpression implements QueryExpression {
        private String query;

        public SqlQueryExpression(String query) {
            this.query = query;
        }

    public String getQueryString() {
            return query;
        }

        public String getBackendQueryString() {
            return query;
        }

        public QueryType getType() {
            return QueryType.ADVANCED_QUERY;
        }

        public boolean isMoreLikeThis() {
            return false;
        }
    }

