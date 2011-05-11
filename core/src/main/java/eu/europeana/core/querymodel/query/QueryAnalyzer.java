/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.querymodel.query;

import eu.delving.core.util.ThemeInterceptor;
import eu.delving.metadata.RecordDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The query is an advanced query when the query string contains - " AND ", " OR ", " NOT " (both uppercase) - a fielded
 * query (detected by the use of a : seperating field and query), e.g. title:"something - a word or phrase prefixed by +
 * or -
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class QueryAnalyzer {

    public QueryType findSolrQueryType(String query, RecordDefinition recordDefinition) throws EuropeanaQueryException {
        String[] terms = query.split("\\s+");
        for (String term : terms) {
            if (BOOLEAN_KEYWORDS.contains(term)) {
                return QueryType.ADVANCED_QUERY;
            }
            else if (term.contains("*:*")) {
                return QueryType.ADVANCED_QUERY;
            }
            else if (term.startsWith("+") || term.startsWith("-")) {
                return QueryType.ADVANCED_QUERY;
            }
            else if (term.indexOf(':') > 0) {
                int colon = term.indexOf(':');
                String field = term.substring(0, colon);
                if ("europeana_uri".equals(field)) {
                    return QueryType.MORE_LIKE_THIS_QUERY;
                }
                else {
                    if (recordDefinition.getFieldNameList().contains(field)) {
                        return QueryType.ADVANCED_QUERY;
                    }
                    else if ("tag".equalsIgnoreCase(field)) {
                        return QueryType.ADVANCED_QUERY;
                    }
                    else if ("text".equalsIgnoreCase(field)) {
                        return QueryType.ADVANCED_QUERY;
                    }
                    else {
                        throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString());
                    }
                }
            }
        }
        return QueryType.SIMPLE_QUERY;
    }

    private List<String> extractParts(String query) {
        List<String> answer = new ArrayList<String>();
        String[] queryParts = query.split("\\s+");
        for (String part : queryParts) {
            if (BOOLEAN_KEYWORDS.contains(part)) {
                part = part.toUpperCase();
            }
            StringBuilder out = new StringBuilder();
            for (int walk = 0; walk < part.length(); walk++) {
                char ch = part.charAt(walk);
                switch (ch) {
                    case '{':
                    case '}':
                        break;
                    default:
                        out.append(ch);
                }
            }
            if (out.length() > 0) {
                answer.add(out.toString());
            }
        }
        return answer;
    }

    public String sanitizeAndTranslate(String query, Locale locale) {
        StringBuilder out = new StringBuilder();
        for (String part : extractParts(query)) {
            int colon = part.indexOf(":");
            if (colon < 0) {
                out.append(part).append(" ");
            }
            else {
                String facet = part.substring(0, colon);
                String value = part.substring(colon+1);
                facet = ThemeInterceptor.getLookup().toFieldName(facet, locale);
                out.append(facet).append(":").append(value).append(" ");
            }
        }
        return out.toString().trim();
    }

    /**
     * Create advanced query from params with facet[1-3], operator[1-3], query[1-3].
     * <p/>
     * This query is structured by the advanced search pane in the portal
     *
     * @param params request parameters
     * @param locale where
     * @return all parameters formatted as a single Lucene Query
     */

    public String createAdvancedQuery(Map<String, String[]> params, Locale locale) {
        StringBuilder queryString = new StringBuilder();
        for (int i = 1; i < 4; i++) {
            String queryKey = String.format("query%d", i);
            String facetKey = String.format("facet%d", i);
            String operatorKey = String.format("operator%d", i);
            if (params.containsKey(queryKey) && params.containsKey(facetKey)) {
                String facet = params.get(facetKey)[0];
                String query = params.get(queryKey)[0];
                String operator = null;
                if (i != 1) {
                    operator = params.get(operatorKey)[0];
                }
                if (!query.isEmpty()) {
                    if (operator != null) {
                        queryString.append(" ").append(operator).append(" ");
                    }
                    if (!facet.isEmpty()) {
                        queryString.append(facet);
                    }
                    else {
                        queryString.append("text");
                    }
                    queryString.append(":").append(query);
                }
            }
        }
        return sanitizeAndTranslate(queryString.toString(), locale);
    }

    public String createRefineSearchFilterQuery(Map<String, String[]> params, Locale locale) throws EuropeanaQueryException {
        String refineQuery = params.get("rq")[0];
        // check length
        String newQuery = "";
        if (refineQuery.trim().length() > 0) {
            if (refineQuery.contains(":")) {
                newQuery = refineQuery;
            }
            else {
                newQuery = String.format("text:\"%s\"", refineQuery);
            }
        }
        return sanitizeAndTranslate(newQuery, locale);
    }

    private static final Set<String> BOOLEAN_KEYWORDS = new TreeSet<String>();

    private static void addBooleanKeyword(String keyword) {
        BOOLEAN_KEYWORDS.add(keyword);
        BOOLEAN_KEYWORDS.add(keyword.toLowerCase());
    }

    static {
        addBooleanKeyword("AND");
        addBooleanKeyword("OR");
        addBooleanKeyword("NOT");
    }



}
