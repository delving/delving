package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class FacetQueryLinksSpec extends Spec with ShouldMatchers {

  /*
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
   */
}