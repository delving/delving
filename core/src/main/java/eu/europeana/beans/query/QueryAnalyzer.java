/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.beans.query;

import eu.europeana.beans.annotation.AnnotationProcessor;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryProblem;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Make decisions about how the query has been constructed.
 * <p/>
 * The query is an advanced query when the query string contains
 * - " AND ", " OR ", " NOT " (both uppercase)
 * - a fielded query (detected by the use of a : seperating field and query), e.g. title:"something
 * - a word or phrase prefixed by + or -
 * - todo: find out if dismax (simple query) handles phrase queries
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class QueryAnalyzer {

    private static final Pattern OR_PATTERN = Pattern.compile("\\s+[oO][rR]\\s+");
    private static final Pattern AND_PATTERN = Pattern.compile("\\s+[aA][nN][dD]\\s+");
    private static final Pattern NOT_START_PATTERN = Pattern.compile("^\\s*[nN][oO][tT]\\s+");
    private static final Pattern NOT_MIDDLE_PATTERN = Pattern.compile("\\s+[nN][oO][tT]\\s+");
    private AnnotationProcessor annotationProcessor;

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    public QueryType findSolrQueryType(String query) throws EuropeanaQueryException {
        String[] terms = query.split("\\s+");
        for (String term : terms) {
            if (BOOLEAN_KEYWORDS.contains(term)) {
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
                else { // todo: check if the field is known?
                    if (annotationProcessor.getSolrFieldList().contains(field)) {
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

    public String sanitize(String query) {
        String[] terms = query.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String term : terms) {
            boolean emptyTerm = true;
            for (int walk = 0; walk < term.length(); walk++) {
                char ch = term.charAt(walk);
                switch (ch) {
                    case '{':
                    case '}':
                        break;
                    default:
                        out.append(ch);
                        emptyTerm = false;
                }
            }
            if (!emptyTerm) {
                out.append(' ');
            }
        }
        String q = out.toString();
        q = AND_PATTERN.matcher(q).replaceAll(" AND ");
        q = OR_PATTERN.matcher(q).replaceAll(" OR ");
        q = NOT_START_PATTERN.matcher(q).replaceAll("NOT ");
        q = NOT_MIDDLE_PATTERN.matcher(q).replaceAll(" NOT ");
        return q.trim();
    }

    private static final Set<String> BOOLEAN_KEYWORDS = new TreeSet<String>();

    private static void add(String keyword) {
        BOOLEAN_KEYWORDS.add(keyword);
        BOOLEAN_KEYWORDS.add(keyword.toLowerCase());
    }

    static {
        add("AND");
        add("OR");
        add("NOT");
    }
}
