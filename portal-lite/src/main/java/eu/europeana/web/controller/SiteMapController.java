package eu.europeana.web.controller;

import eu.europeana.database.DashboardDao;
import eu.europeana.query.SitemapIndexEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 5, 2010 10:48:27 PM
 */

public class SiteMapController {

    // todo this class must be completely rewritten to use Solr instead of database to file the sitemap

    private static final int MAX_RECORDS_PER_SITEMAP_FILE = 45000;

	private static final String EUROPEANA_URL = "http://europeana.eu/portal/";

	private static final String PREFIX_FULL_VIEW = EUROPEANA_URL + "full-doc.html?uri=";

	@Autowired
    DashboardDao dashboardDao;

	@RequestMapping("/sitemap.xml")
	public ModelAndView handleSitemap(
			@RequestParam(value = "collection", required = false) String collection,
			@RequestParam(value = "page", required = false) String page,
			HttpServletRequest request
	) throws Exception {

		Format dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
		ModelAndView mavPage = null;

//		if (collection == null) {
//
//			// generate sitemap_index
//            // Use solr index instead query *:* with Facet collectionName
//            // use result list to iterate over all collections
//
//			List<EuropeanaCollection> europeanaCollections = dashboardDao.fetchCollections();
//			for (EuropeanaCollection europeanaCollection : europeanaCollections) {
//				for (int i = 0; i <= europeanaCollection.getTotalRecords() / MAX_RECORDS_PER_SITEMAP_FILE; i++) {
//					entries.add(
//							new SitemapIndexEntry(
//									StringEscapeUtils.escapeXml(EUROPEANA_URL + "sitemap.xml?collection=" + europeanaCollection.getName() + "&page=" + i),
//									dateFormatter.format(europeanaCollection.getCollectionLastModified())));
//				}
//			}
//
//			mavPage = ControllerUtil.createModelAndViewPage("sitemap-index-xml");
//		} else {
//
//			// generate sitemap for a collection
//			if (page != null && page.length() > 0 && page.length() < 3 && NumberUtils.isDigits(page)) {
//
//				EuropeanaCollection europeanaCollection = dashboardDao.fetchCollectionByName(collection, false);
//				int maxPageForCollection = europeanaCollection.getTotalRecords() / MAX_RECORDS_PER_SITEMAP_FILE + 1;
//				int pageInt = Integer.parseInt(page);
//
//				if (pageInt <= maxPageForCollection) {
//
//					// dump this page of records
//					List<EuropeanaId> collectionObjects = dashboardDao.fetchCollectionObjects(europeanaCollection);
//					for (
//							int i = pageInt * MAX_RECORDS_PER_SITEMAP_FILE;
//							i < (pageInt + 1) * MAX_RECORDS_PER_SITEMAP_FILE && i < collectionObjects.size();
//							i++) {
//
//						entries.add(
//								new SitemapEntry(
//										StringEscapeUtils.escapeXml(PREFIX_FULL_VIEW + collectionObjects.get(i).getEuropeanaUri()),
//										dateFormatter.format(europeanaCollection.getCollectionLastModified()),
//										"monthly",
//										"0.5"
//								));
//					}
//				}
//
//			}
//			mavPage = ControllerUtil.createModelAndViewPage("sitemap-xml");
//		}
//		mavPage.addObject("entries", entries);
		return mavPage;
	}
}
