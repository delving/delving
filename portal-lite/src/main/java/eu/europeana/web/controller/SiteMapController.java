package eu.europeana.web.controller;

import eu.europeana.beans.IdBean;
import eu.europeana.beans.query.SiteMapBeanView;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.query.QueryModelFactory;
import eu.europeana.web.util.ControllerUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
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
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 5, 2010 10:48:27 PM
 */

@Controller
public class SiteMapController {

    private static final int MAX_RECORDS_PER_SITEMAP_FILE = 1000;

    @Value("#{europeanaProperties['displayPageUrl']}")
    private String fullViewUrl;

    @Autowired
    private QueryModelFactory beanQueryModelFactory;

    @Autowired
    DashboardDao dashboardDao;

    @RequestMapping("/sitemap.xml")
    public ModelAndView handleSitemap(
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "page", required = false) String page,
            HttpServletRequest request
    ) throws Exception {
        ModelAndView mavPage;
        String fullDocPageString = "full-doc.html";
        if (fullViewUrl.endsWith(fullDocPageString)) {
            fullViewUrl = fullViewUrl.substring(0, fullViewUrl.length() - fullDocPageString.length());
        }
        if (collection == null) {
            List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
            List<EuropeanaCollection> europeanaCollections = dashboardDao.fetchCollections();
            for (EuropeanaCollection europeanaCollection : europeanaCollections) {
                for (int i = 0; i <= europeanaCollection.getTotalRecords() / MAX_RECORDS_PER_SITEMAP_FILE; i++) {
                    // add each page of a collection to the index.
                    entries.add(
                            new SitemapIndexEntry(
                                    StringEscapeUtils.escapeXml(String.format("%ssitemap.xml?collection=%s&page=%d", fullViewUrl, europeanaCollection.getName(), i)),
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
                    List<IdBean> list = siteMapBeanView.getIdBeans();
                    mavPage.addObject("idBeanList", list);
                }
            }
        }
        return mavPage;
    }

    /**
     * Sitemap index entry, model for MVC.
     *
     * @author Borys Omelayenko
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
