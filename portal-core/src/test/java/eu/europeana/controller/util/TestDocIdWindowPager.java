package eu.europeana.controller.util;

import eu.europeana.json.solr.SolrQueryModelFactory;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import org.apache.commons.httpclient.HttpClient;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Dec 15, 2008: 9:55:04 PM
 */

// todo: implement this is a reliable way

public class TestDocIdWindowPager {
    private QueryModel queryModel;
    private MockHttpServletRequest request;
    private static SolrQueryModelFactory factory;

    @BeforeClass
    public static void setUp() throws IOException {
        factory = new SolrQueryModelFactory();
        factory.setHttpClient(new HttpClient());
        factory.setBaseUrl("http://localhost:8983/solr/select"); //TODO
    }

    @Before
    public void beforeTest() {
        queryModel = factory.createQueryModel(QueryModelFactory.SearchType.SIMPLE);
        request = new MockHttpServletRequest();
    }

    @After
    public void afterTest() {
        request.close();
    }

    @Test
    public void hasNextAndPreviousTest() throws UnsupportedEncodingException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/74108B5CC7D4A0B86C7C5E53EC8300F17ED9AF2D";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "2");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "bd");
        DocIdWindowPager pager = new DocIdWindowPager(uri, request, queryModel);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getNextInt(), 3);
        assertEquals(pager.getPreviousInt(), 1);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        System.out.println(pager.toString());
    }


    @Test
    public void hasNoPrevious() throws UnsupportedEncodingException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "1");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "bd");
        DocIdWindowPager pager = new DocIdWindowPager(uri, request, queryModel);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getNextInt(), 2);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

    @Test
    public void hasNoNext() throws UnsupportedEncodingException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "13");
        request.addParameter("start", "14");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "bd");
        DocIdWindowPager pager = new DocIdWindowPager(uri, request, queryModel);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.getPreviousInt(), 13);
        assertEquals(pager.getDocIdWindow().getIds().size(), 2);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

    @Test
    public void arrayOutOfBounds() throws UnsupportedEncodingException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("start", "18");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "bd");
        DocIdWindowPager pager = new DocIdWindowPager(uri, request, queryModel);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 0);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }

    @Test
    public void returnToBob() throws UnsupportedEncodingException, EuropeanaQueryException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("start", "1");
        request.addParameter("query", "1920 max devrient");
        request.addParameter("pageId", "yg");
        DocIdWindowPager pager = new DocIdWindowPager(uri, request, queryModel);
        assertEquals(pager.getReturnToResults(), "year-grid.html?query=max+devrient&bq=1920+max+devrient&start=1&view=table&tab=null");
        assertEquals(pager.isNext(), true);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }

}
