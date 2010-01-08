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

import eu.europeana.database.domain.CarouselItem;
import eu.europeana.query.Facet;
import eu.europeana.query.FacetCount;
import eu.europeana.query.FacetType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.*;

/**
 * Test the utility classes
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Vitali Kiruta
 * @author Sjoerd Siebinga  <sjoerd.siebinga@gmail.com>
 */

public class TestUtilities {

    @Test
    public void testPickShuffledRandomItems() {
        List<CarouselItem> cache = fillCarouselItemList();
        CarouselItemSampler sampler = new CarouselItemSampler();
        sampler.setDisplayCount(3);
        sampler.setCache(cache);
        List<CarouselItem> previous = sampler.pickShuffledRandomItems();
        for (int i = 0; i < 10; i++) {
            assertNotNull(previous);
            List<CarouselItem> next = sampler.pickShuffledRandomItems();
            assertNotNull(next);
            assertNotSame("Next and Previous shuffeled list should not be the same", next, previous);
            previous = next;
        }
    }

    private List<CarouselItem> fillCarouselItemList() {
        List<CarouselItem> dummylist = new ArrayList<CarouselItem>();
        for (int i = 0; i < 40; i++) {
            CarouselItem item = new CarouselItem();
            item.setEuropeanaUri("uri-" + i);
            item.setTitle("title: " + i);
            dummylist.add(item);
        }
        return dummylist;
    }

    @Test
    public void queryConstraints() throws Exception {
        String [] constraintStrings = new String[] {
                "YEAR:1900",
                "YEAR:1901",
                "LOCATION:Here",
        };
        QueryConstraints qc = new QueryConstraints(constraintStrings);
        assertEquals(qc.toQueryString(), "&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here");
        List<QueryConstraints.Breadcrumb> crumbs = qc.toBreadcrumbs("query","kultur");
        String [][] expect = new String[][] {
                {"query=kultur", "kultur", "false" },
                {"query=kultur&qf=YEAR:1900", "YEAR:1900", "false" },
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901", "YEAR:1901", "false"},
                {"query=kultur&qf=YEAR:1900&qf=YEAR:1901&qf=LOCATION:Here", "LOCATION:Here", "true" },
        };
        int index = 0;
        for (QueryConstraints.Breadcrumb crumb : crumbs) {
            assertEquals(crumb.getHref(),expect[index][0]);
            assertEquals(crumb.getDisplay(),expect[index][1]);
            assertEquals(String.valueOf(crumb.getLast()),expect[index][2]);
            index++;
        }
    }

    @Test
    public void smallPagination() throws Exception {
        String page = makePage(30, 20, 1);
        assertEquals(
                "1\n" +
                        "2(21)\n" +
                        "next(21)\n",
                page
        );
    }

    @Test
    public void startPagination() throws Exception {
        String page = makePage(300, 20, 1);
        assertEquals(
                "1\n" +
                        "2(21)\n" +
                        "3(41)\n" +
                        "4(61)\n" +
                        "5(81)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "next(21)\n",
                page
        );
    }

    @Test
    public void middlePagination() throws Exception {
        String page = makePage(300, 20, 141);
        assertEquals(
                "prev(121)\n" +
                        "3(41)\n" +
                        "4(61)\n" +
                        "5(81)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "next(161)\n",
                page
        );
    }

    @Test
    public void nearEndPagination() throws Exception {
        String page = makePage(300, 20, 261);
        assertEquals(
                "prev(241)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "13(241)\n" +
                        "14\n" +
                        "15(281)\n" +
                        "next(281)\n",
                page
        );

    }

    @Test
    public void endPagination() throws Exception {
        String page = makePage(300, 20, 281);
        assertEquals(
                "prev(261)\n" +
                        "6(101)\n" +
                        "7(121)\n" +
                        "8(141)\n" +
                        "9(161)\n" +
                        "10(181)\n" +
                        "11(201)\n" +
                        "12(221)\n" +
                        "13(241)\n" +
                        "14(261)\n" +
                        "15\n",
                page
        );
    }

    private String makePage(int numFound, int rows, int start) throws Exception {
        return makePage(new ResultPaginationImpl(numFound, rows, start));
    }

    private String makePage(ResultPaginationImpl p) throws Exception {
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("pagination", p);
        return FreemarkerUtil.processResource("/example-pagination.ftl", model);
    }

    @Test
    public void nextQueryFacet() throws Exception {
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
                assertEquals(expect[index++], link.toString());
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
