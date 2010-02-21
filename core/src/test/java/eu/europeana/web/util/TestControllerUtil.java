/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.util;

import eu.europeana.core.querymodel.query.SolrQueryUtil;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Dec 13, 2009 11:03:59 AM
 */
public class TestControllerUtil {

    private Logger log = Logger.getLogger(getClass());

    @Before
    public void testInit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

    }

    @Test
    @Ignore("Implement first")
    public void testFormatFullRequestUrl() throws Exception {
    }

    @Test
    public void testFormatParameterMapAsQueryString() throws Exception {
        Map<String, String> mockParameterMap = new HashMap<String, String>();
        mockParameterMap.put("query", "sjoerd");
        mockParameterMap.put("id", "007");
        mockParameterMap.put("className", "SavedSearch");
        Assert.assertEquals("The formatted string should be the same",
                "?id=007&query=sjoerd&className=SavedSearch",
                ControllerUtil.formatParameterMapAsQueryString(mockParameterMap));

    }

    @Test
    public void testPhraseConversions() {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFilterQueries(
                "PROVIDER:The European Library",
                "LANGUAGE:en"
        );
        String[] phrased = SolrQueryUtil.getFilterQueriesAsPhrases(solrQuery);
        for (String s : phrased) {
            String after = s.substring(s.indexOf(':') + 1);
            Assert.assertTrue(after.startsWith("\"") && after.endsWith("\""));
        }
        solrQuery.setFilterQueries(phrased);
        String [] unphrased = SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery);
        for (String s : unphrased) {
            String after = s.substring(s.indexOf(':') + 1);
            Assert.assertFalse(after.startsWith("\"") && after.endsWith("\""));
        }
    }

    @Test
    public void testOrBasedFacetQueries() throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFilterQueries(
                "PROVIDER:\"The European Library\"",
                "LANGUAGE:\"en\"",
                "LANGUAGE:\"de\""
        );
        Map<String, String> facetMap = new HashMap<String, String>();
        facetMap.put("PROVIDER", "prov");
        facetMap.put("LANGUAGE", "lang");
        String[] expectFilterQueries = new String[]{"{!tag=lang}LANGUAGE:(\"de\" OR \"en\")", "{!tag=prov}PROVIDER:\"The European Library\""};
        log.info("expecting:");
        for (String expect : expectFilterQueries) {
            log.info(expect);
        }
        String[] actualFilterQueries = SolrQueryUtil.getFilterQueriesAsOrQueries(solrQuery, facetMap);
        log.info("actual:");
        for (String actual : actualFilterQueries) {
            log.info(actual);
        }
        Assert.assertArrayEquals("arrays should be equal", expectFilterQueries, actualFilterQueries);
    }
}
