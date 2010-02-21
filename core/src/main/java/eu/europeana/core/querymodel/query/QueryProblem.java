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

package eu.europeana.core.querymodel.query;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public enum QueryProblem {
    MATCH_ALL_DOCS("org.apache.lucene.search.MatchAllDocsQuery"),
    UNDEFINED_FIELD("Undefined field"),
    RECORD_NOT_INDEXED("Requested Europeana record not indexed."),
    RECORD_NOT_FOUND("Requested Europeana record not found."),
    RECORD_REVOKED("Europeana record is revoked by the content provider."),
    MALFORMED_URL("Required parameters are missing from the request."),
    MALFORMED_QUERY("Query to Search Engine is malformed."),
    UNKNOWN("unknown"),
    UNABLE_TO_CHANGE_LANGUAGE("We are unable to change the interface to the requested language."),
    TOKEN_EXPIRED("Europeana token has expired and is no longer valid."),
    UNKNOWN_TOKEN("Token does not exist."),
    SOLR_UNREACHABLE("Unable to reach Solr Search Engine."),
    UNABLE_TO_PARSE_JSON("Unable to parse JSON response."),
    NONE("An exception occurred"); // Move on, people, nothing to see here.


    private String fragment;

    QueryProblem(String fragment) {
        this.fragment = fragment;
    }

    public static QueryProblem get(String message) {
        for (QueryProblem problem : values()) {
            if (message.equalsIgnoreCase(problem.toString())) {
                return problem;
            }
        }
        return UNKNOWN;
    }

    public String getFragment() {
        return fragment;
    }
}