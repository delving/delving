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

package eu.europeana.web.controller;

import eu.europeana.core.database.DashboardDao;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.querymodel.query.DocId;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryModelFactory;
import eu.europeana.core.querymodel.query.SiteMapBeanView;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Sitemap controller for generating sitemap.xml: sitemap and index.
 *
 * @author Borys Omelayenko <borys.omelayenko@kb.nl>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 5, 2010 10:48:27 PM
 */

@Controller
public class SitemapController {

    // don't increase this higher then 1000. It will put undue strain on SearchEngine
    private static final int MAX_RECORDS_PER_SITEMAP_FILE = 1000;

    @Value("#{europeanaProperties['displayPageUrl']}")
    private String fullViewUrl;

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    @Autowired
    DashboardDao dashboardDao;

//    @RequestMapping("/sitemap.xml")
    public ModelAndView handleSitemap(
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "page", required = false) String page,
            HttpServletRequest request
    ) throws SolrServerException, EuropeanaQueryException {
        String fullDocPageString = "full-doc.html";
        String baseUrl = fullViewUrl;
        if (baseUrl.endsWith(fullDocPageString)) {
            baseUrl = baseUrl.substring(0, fullViewUrl.length() - fullDocPageString.length());
        }
        ModelAndView mavPage;
        if (collection == null) {
            List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
            // todo maybe this query must be cached because it might be too heavy a query on the database
            List<EuropeanaCollection> europeanaCollections = dashboardDao.fetchEnabledCollections();
            for (EuropeanaCollection europeanaCollection : europeanaCollections) {
                for (int i = 0; i <= europeanaCollection.getTotalRecords() / MAX_RECORDS_PER_SITEMAP_FILE; i++) {
                    // add each page of a collection to the index.
                    entries.add(
                            new SitemapIndexEntry(
                                    StringEscapeUtils.escapeXml(String.format("%ssitemap.xml?collection=%s&page=%d", baseUrl, europeanaCollection.getName(), i)),
                                    europeanaCollection.getCollectionLastModified()));
                }
            }

            mavPage = ControllerUtil.createModelAndViewPage("sitemap-index");
            mavPage.addObject("entries", entries);
        }
        else {
            mavPage = ControllerUtil.createModelAndViewPage("sitemap");
            mavPage.addObject("fullViewUrl", fullViewUrl);

            // generate sitemap for a collection
            if (page != null && page.length() > 0 && page.length() < 4 && NumberUtils.isDigits(page)) {
                int pageInt = Integer.parseInt(page);
                SiteMapBeanView siteMapBeanView = beanQueryModelFactory.getSiteMapBeanView(collection, MAX_RECORDS_PER_SITEMAP_FILE, pageInt);
                int maxPageForCollection = siteMapBeanView.getMaxPageForCollection();
                if (pageInt <= maxPageForCollection) {
                    List<? extends DocId> list = siteMapBeanView.getIdBeans();
                    mavPage.addObject("idBeanList", list);
                }
            }
        }
        return mavPage;
    }

     /**
     * This is a test method to determine the load crawl bots put on the system and to see how a test collection
     * "tel-treasures" is discoverable via the web search engine
     *
     * @param collection
     * @param page
     * @param request
     * @return ModelAndView
     * @throws org.apache.solr.client.solrj.SolrServerException
     * @throws eu.europeana.core.querymodel.query.EuropeanaQueryException
     */
    @RequestMapping("/sitemap.xml")
    public ModelAndView handleTestSitemap(
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "page", required = false) String page,
            HttpServletRequest request
    ) throws SolrServerException, EuropeanaQueryException {
        String fullDocPageString = "full-doc.html";
        String baseUrl = fullViewUrl;
        if (baseUrl.endsWith(fullDocPageString)) {
            baseUrl = baseUrl.substring(0, fullViewUrl.length() - fullDocPageString.length());
        }
        ModelAndView mavPage;
        if (collection == null) {
            List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
            EuropeanaCollection testCollection = new EuropeanaCollection();
            testCollection.setName("92001_Ag_EU_TELtreasures ");
            List<EuropeanaCollection> europeanaCollections = new ArrayList<EuropeanaCollection>();
            europeanaCollections.add(testCollection);
            for (EuropeanaCollection europeanaCollection : europeanaCollections) {
                for (int i = 0; i <= europeanaCollection.getTotalRecords() / MAX_RECORDS_PER_SITEMAP_FILE; i++) {
                    // add each page of a collection to the index.
                    entries.add(
                            new SitemapIndexEntry(
                                    StringEscapeUtils.escapeXml(String.format("%ssitemap.xml?collection=%s&page=%d", baseUrl, europeanaCollection.getName(), i)),
                                    europeanaCollection.getCollectionLastModified()));
                }
            }

            mavPage = ControllerUtil.createModelAndViewPage("sitemap-index");
            mavPage.addObject("entries", entries);
        }
        else {
            mavPage = ControllerUtil.createModelAndViewPage("sitemap");
            mavPage.addObject("fullViewUrl", fullViewUrl);

            // generate sitemap for a collection
            if (page != null && page.length() > 0 && page.length() < 4 && NumberUtils.isDigits(page)) {
                int pageInt = Integer.parseInt(page);
                SiteMapBeanView siteMapBeanView = beanQueryModelFactory.getSiteMapBeanView(collection, MAX_RECORDS_PER_SITEMAP_FILE, pageInt);
                int maxPageForCollection = siteMapBeanView.getMaxPageForCollection();
                if (pageInt <= maxPageForCollection) {
                    List<? extends DocId> list = siteMapBeanView.getIdBeans();
                    mavPage.addObject("idBeanList", list);
                }
            }
        }
        return mavPage;
    }

    /**
     * Sitemap index entry, model for MVC.
     */
    public static class SitemapIndexEntry {

        private String loc;
        private final Date lastmod;

        public String getLoc() {
            return loc;
        }

        public Date getLastmod() {
            return lastmod;
        }

        public SitemapIndexEntry(String loc, Date lastmod) {
            this.loc = loc;
            this.lastmod = lastmod;
        }

    }

}
