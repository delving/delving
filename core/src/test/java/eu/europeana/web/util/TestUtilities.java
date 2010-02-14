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
import org.apache.solr.client.solrj.SolrQuery;
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
    public void smallPagination() throws Exception {
        String page = makePage(30, 20, 0);
        assertEquals(
                "1\n" +
                        "2(21)\n" +
                        "next(21)\n",
                page
        );
    }

    @Test
    public void startPagination() throws Exception {
        String page = makePage(300, 20, 0);
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
        String page = makePage(300, 20, 140);
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
        String page = makePage(300, 20, 260);
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
        String page = makePage(300, 20, 280);
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
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("");
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        return makePage(new ResultPaginationImpl(solrQuery, numFound, "queryString??"));
    }

    private String makePage(ResultPaginationImpl p) throws Exception {
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("pagination", p);
        return FreemarkerUtil.processResource("/example-pagination.ftl", model);
    }
}
