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

import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.fixture.IngestionFixture;
import eu.europeana.incoming.ImportFile;
import eu.europeana.query.DocIdWindowPager;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Dec 15, 2008: 9:55:04 PM
 */

// todo: implement this is a reliable way

public class TestDocIdWindowPager {
    private Logger log = Logger.getLogger(TestDocIdWindowPager.class);

    private MockHttpServletRequest request;

    @Autowired
    private IngestionFixture ingestionFixture;

    @Autowired
    private DashboardDao dashboardDao;

    private static EuropeanaCollection collection;
    private static ImportFile importFile;
    private static SolrServer solrServer; // todo

    @Before
    public void init() throws Exception {
        if (collection == null) {
            ingestionFixture.deleteImportRepository();
            log.info("=== initializing test ingestion ===");
            List<ImportFile> importFiles = ingestionFixture.getImportRepository().getAllFiles();
            Assert.assertEquals(1, importFiles.size());
            importFile = importFiles.get(0);
            log.info(importFile);
            collection = dashboardDao.fetchCollection(importFile.deriveCollectionName(), importFile.getFileName(), true);
            log.info(collection.getName());
            ingestionFixture.startSolr();
        }
    }

    @After
    public void cleanup() throws Exception {
//        ingestionFixture.stopSolr();
    }

//    @Test
    public void hasNextAndPreviousTest() throws SolrServerException {
        String uri = "http://www.europeana.eu/resolve/record/900/74108B5CC7D4A0B86C7C5E53EC8300F17ED9AF2D";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "2");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getNextInt(), 3);
        assertEquals(pager.getPreviousInt(), 1);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        System.out.println(pager.toString());
    }


//    @Test
    public void hasNoPrevious() throws SolrServerException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "1");
        request.addParameter("start", "1");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.isNext(), true);
        assertEquals(pager.getNextInt(), 2);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

//    @Test
    public void hasNoNext() throws SolrServerException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("startPage", "13");
        request.addParameter("start", "14");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), true);
        assertEquals(pager.getPreviousInt(), 13);
        assertEquals(pager.getDocIdWindow().getIds().size(), 2);
        assertEquals(pager.getFullDocUri(), uri);
        System.out.println(pager.toString());
    }

//    @Test
    public void arrayOutOfBounds() throws SolrServerException {
        String uri = "http://www.europeana.eu/resolve/record/900/5B2BC9E71B33BAC133EBBF4A8EC0594B71D2103F";
        request.addParameter("uri", uri);
        request.addParameter("start", "18");
        request.addParameter("query", "max devrient");
        request.addParameter("pageId", "brd");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer);
        assertEquals(pager.isNext(), false);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 0);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }

//    @Test
    public void returnToBob() throws SolrServerException {
        String uri = "http://www.europeana.eu/resolve/record/900/2E27B23C3161A60AA13212D2991AB9E5F7226977";
        request.addParameter("uri", uri);
        request.addParameter("start", "1");
        request.addParameter("query", "1920 max devrient");
        request.addParameter("pageId", "yg");
        SolrQuery solrQuery = new SolrQuery();// todo
        DocIdWindowPager pager = DocIdWindowPagerImpl.fetchPager(request.getParameterMap(), solrQuery, solrServer);
        assertEquals(pager.getReturnToResults(), "year-grid.html?query=max+devrient&bq=1920+max+devrient&start=1&view=table&tab=null");
        assertEquals(pager.isNext(), true);
        assertEquals(pager.isPrevious(), false);
        assertEquals(pager.getDocIdWindow().getIds().size(), 3);
        assertEquals(pager.getFullDocUri(), uri);
        assertEquals(pager.getQueryStringForPaging().endsWith("1"), true);
        System.out.println(pager.toString());
    }

}
