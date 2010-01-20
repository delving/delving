/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Make sure the query analyzer understands the different query types
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestQueryAnalyzer {

    private QueryAnalyzer qa = new QueryAnalyzer();

    @Test
    public void variety() throws Exception {
        simple("very simple query");
        advanced("advanced and Query");
        advanced("text:fieldedQuery");
        advanced("+\"phrase query\"");
        moreLike("europeana_uri:\"http://www.europeana.eu/bla/bla\"");
    }

    @Test
    public void sanitize() throws Exception {
        sanitize("hello { and { goodbye }", "hello AND goodbye");
    }

    private void sanitize(String from, String to) {
        assertEquals("Not sanitized properly", to, qa.sanitize(from));
    }

    private void simple(String query) {
        assertEquals("Not a simple query ["+query+"]", QueryType.SIMPLE_QUERY, qa.findSolrQueryType(query));
    }

    private void advanced(String query) {
        assertEquals("Not an advanced query ["+query+"]", QueryType.ADVANCED_QUERY, qa.findSolrQueryType(query));
    }

    private void moreLike(String query) {
        assertEquals("Not a more-like query ["+query+"]", QueryType.MORE_LIKE_THIS_QUERY, qa.findSolrQueryType(query));
    }


}
