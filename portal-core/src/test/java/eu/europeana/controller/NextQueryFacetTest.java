package eu.europeana.controller;

import eu.europeana.controller.util.NextQueryFacet;
import eu.europeana.controller.util.QueryConstraints;
import eu.europeana.query.Facet;
import eu.europeana.query.FacetCount;
import eu.europeana.query.FacetType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test the NextQueryLink building
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NextQueryFacetTest {
    @Test
    public void performRequest() throws Exception {
        List<Facet> facets = new ArrayList<Facet>();
        Facet facet = new FacetImpl(FacetType.LANGUAGE);
        facet.getCounts().add(new FacetCountImpl("en"));
        facet.getCounts().add(new FacetCountImpl("de"));
        facet.getCounts().add(new FacetCountImpl("nl"));
        facets.add(facet);
        facet = new FacetImpl(FacetType.YEAR);
        facet.getCounts().add(new FacetCountImpl("1980"));
        facet.getCounts().add(new FacetCountImpl("1981"));
        facet.getCounts().add(new FacetCountImpl("1982"));
        String [] activeFacets = new String[] {
                "LANGUAGE:de",
                "LANGUAGE:nl",
                "YEAR:1980"
        };
        facets.add(facet);
        List<NextQueryFacet> facetLinks = NextQueryFacet.createDecoratedFacets(facets, new QueryConstraints(activeFacets));
        StringBuilder url = new StringBuilder();
        for (String activeFacet : activeFacets) {
            url.append("&qf=");
            url.append(activeFacet);
        }
        System.out.println("original: "+url);
        System.out.println();
        String [] expect = new String [] {
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=LANGUAGE:en'>en</a> (add)",
                "<a href='&qf=LANGUAGE:nl&qf=YEAR:1980'>de</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=YEAR:1980'>nl</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl'>1980</a> (remove)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1981'>1981</a> (add)",
                "<a href='&qf=LANGUAGE:de&qf=LANGUAGE:nl&qf=YEAR:1980&qf=YEAR:1982'>1982</a> (add)",
        };
        int index = 0;
        for (NextQueryFacet facetLink : facetLinks) {
            for (NextQueryFacet.FacetCountLink link : facetLink.getLinks()) {
                System.out.println("\""+link+"\",");
                Assert.assertEquals(expect[index++], link.toString());
            }
        }
    }

    private class FacetImpl implements Facet {
        private FacetType facetType;
        private List<FacetCount> counts = new ArrayList<FacetCount>();

        private FacetImpl(FacetType facetType) {
            this.facetType = facetType;
        }

        public FacetType getType() {
            return facetType;
        }

        public List<FacetCount> getCounts() {
            return counts;
        }
    }

    private class FacetCountImpl implements FacetCount {
        private String value;

        private FacetCountImpl(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Integer getCount() {
            return 0;
        }
    }
}