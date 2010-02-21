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

import eu.europeana.core.querymodel.query.Breadcrumb;
import eu.europeana.core.querymodel.query.FacetQueryLinks;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test the FacetQueryLinks
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestLinkGenerators {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void facetQueryLinks() throws Exception {
        log.info("facet query links");
        List<FacetField> facets = new ArrayList<FacetField>();
        FacetField facet = new FacetField("LANGUAGE");
        facet.add("en", 1);
        facet.add("de", 1);
        facet.add("nl", 1);
        facets.add(facet);
        facet = new FacetField("YEAR");
        facet.add("0000", 666); // testing to see if this is indeed ignored
        facet.add("1980", 1);
        facet.add("1981", 1);
        facet.add("1982", 1);
        facets.add(facet);
        facet = new FacetField("TYPE");
        facets.add(facet);
        SolrQuery query = new SolrQuery();
        query.addFacetField("LANGUAGE", "YEAR", "TYPE");
        query.addFilterQuery("LANGUAGE:de");
        query.addFilterQuery("LANGUAGE:nl");
        query.addFilterQuery("YEAR:1980");
        List<FacetQueryLinks> facetLinks = FacetQueryLinks.createDecoratedFacets(query, facets);
        String[] expect = new String[]{
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=LANGUAGE:en'>en</a> (add)",
                "<a href='&qf=LANGUAGE:nl&qf=YEAR:1980'>de</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=YEAR:1980'>nl</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl'>1980</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1981'>1981</a> (add)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1982'>1982</a> (add)",
        };
        int index = 0;
        for (FacetQueryLinks facetLink : facetLinks) {
            if (facetLink.getType().equalsIgnoreCase("TYPE")) {
                assertEquals(false, facetLink.isSelected());
            }
            else {
                assertEquals(true, facetLink.isSelected());
            }
            for (FacetQueryLinks.FacetCountLink link : facetLink.getLinks()) {
                log.info(link);
                assertEquals(expect[index++], link.toString());
            }
        }
    }

    @Test
    @Ignore("It doesn't work with facet queries")
    public void breadcrumbsFromFacetQueries() throws UnsupportedEncodingException {
       SolrQuery solrQuery = new SolrQuery();
        solrQuery.addFacetField("YEAR", "LOCATION");
        solrQuery.addFacetQuery("YEAR:1900");
        solrQuery.addFacetQuery("YEAR:1901");
        solrQuery.addFacetQuery("LOCATION:Here");
        solrQuery.setQuery("kultur");
        List<Breadcrumb> breadcrumbs = Breadcrumb.createList(solrQuery);
        String [][] expect = new String[][] {
                {"query=kultur", "kultur", "false" },
                {"query=kultur&qf=YEAR:1900", "YEAR:1900", "false" },
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901", "YEAR:1901", "false"},
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here", "LOCATION:Here", "true" },
        };
        int index = 0;
        for (Breadcrumb breadcrumb : breadcrumbs) {
            log.info(breadcrumb);
            assertEquals(breadcrumb.getHref(),expect[index][0]);
            assertEquals(breadcrumb.getDisplay(),expect[index][1]);
            assertEquals(String.valueOf(breadcrumb.getLast()),expect[index][2]);
            index++;
        }
    }


    @Test
    public void breadcrumbsFromFilterQueries() throws UnsupportedEncodingException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFilterQueries(
                "YEAR:1900",
                "YEAR:1901",
                "LOCATION:Here"
        );
        solrQuery.setQuery("kultur");
        List<Breadcrumb> breadcrumbs = Breadcrumb.createList(solrQuery);
        String [][] expect = new String[][] {
                {"query=kultur", "kultur", "false" },
                {"query=kultur&qf=YEAR:1900", "YEAR:1900", "false" },
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901", "YEAR:1901", "false"},
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here", "LOCATION:Here", "true" },
        };
        int index = 0;
        for (Breadcrumb breadcrumb : breadcrumbs) {
            log.info(breadcrumb);
            assertEquals(breadcrumb.getHref(),expect[index][0]);
            assertEquals(breadcrumb.getDisplay(),expect[index][1]);
            assertEquals(String.valueOf(breadcrumb.getLast()),expect[index][2]);
            index++;
        }
    }


}