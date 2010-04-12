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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.core.querymodel.query.DocId;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryModelFactory;
import eu.europeana.core.querymodel.query.QueryType;
import eu.europeana.core.querymodel.query.SiteMapBeanView;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;


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
	// reserved for collections of up to 99,999,000 items
	private static final int MAX_LENGTH_PAGE_NUMBER = 6;

	@Value("#{europeanaProperties['displayPageUrl']}")
	private String fullViewUrl;

	@Autowired
	private QueryModelFactory beanQueryModelFactory;

	@Autowired
	private ClickStreamLogger clickStreamLogger;

	@RequestMapping("/europeana-sitemap.xml")
	public ModelAndView handleSitemap(
			@RequestParam(value = "provider", required = false) String provider,
			@RequestParam(value = "page", required = false) String page,
			HttpServletRequest request
	) throws SolrServerException, EuropeanaQueryException {

		String fullDocPageString = "full-doc.html";
		String baseUrl = fullViewUrl;
		if (baseUrl.endsWith(fullDocPageString)) {
			baseUrl = baseUrl.substring(0, fullViewUrl.length() - fullDocPageString.length());
		}
		ModelAndView mavPage;
		if (provider == null) {

			// sitemap index - collections overview
			List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
			for (FacetField.Count facetField : getCollections()) {

				// sitemaps have a limit on links per file (page)
				double numberOfPages = (double) facetField.getCount() / MAX_RECORDS_PER_SITEMAP_FILE;
				if (numberOfPages > 0) {
					int pageCounter = 0;
					do {
						try {
							entries.add(
									new SitemapIndexEntry(
											StringEscapeUtils.escapeXml(
													String.format(
															"%seuropeana-sitemap.xml?provider=%s&page=%d", 
															baseUrl, 
															URLEncoder.encode(facetField.getName(), "UTF-8"), 
															pageCounter)),
											facetField.getName(),
											new Date(), //todo: add more relevant date later
											facetField.getCount()));
						} catch (UnsupportedEncodingException e) {
							Log.warn(e.getMessage() + " on " + facetField.getName());
						} 
						pageCounter++;
					}
					while (pageCounter < numberOfPages);
				}
			}
			mavPage = ControllerUtil.createModelAndViewPage("sitemap-index.xml");
			mavPage.addObject("entries", entries);
		}
		else {

			// a sitemap
			mavPage = ControllerUtil.createModelAndViewPage("sitemap.xml");
			mavPage.addObject("fullViewUrl", fullViewUrl);

			// generate sitemap for a collection
			if (page != null && page.length() > 0 && page.length() < MAX_LENGTH_PAGE_NUMBER && NumberUtils.isDigits(page)) {
				int pageInt = Integer.parseInt(page);

				SiteMapBeanView siteMapBeanView = beanQueryModelFactory.getSiteMapBeanView(provider, MAX_RECORDS_PER_SITEMAP_FILE, pageInt);
				int maxPageForCollection = siteMapBeanView.getMaxPageForCollection();
				if (pageInt <= maxPageForCollection) {
					List<? extends DocId> list = siteMapBeanView.getIdBeans();
					mavPage.addObject("idBeanList", list);
				}
			}
		}
		clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.SITE_MAP_XML, mavPage);
		return mavPage;
	}

	@RequestMapping("/europeana-sitemap.html")
	public ModelAndView handleCollections(
			HttpServletRequest request
	) throws SolrServerException, EuropeanaQueryException {

		String fullDocPageString = "full-doc.html";
		String baseUrl = fullViewUrl;
		if (baseUrl.endsWith(fullDocPageString)) {
			baseUrl = baseUrl.substring(0, fullViewUrl.length() - fullDocPageString.length());
		}
		ModelAndView mavPage;

		// sitemap index - collections overview
		List<SitemapIndexEntry> entries = new ArrayList<SitemapIndexEntry>();
		for (FacetField.Count facetField : getCollections()) {

			try {
				entries.add(
						new SitemapIndexEntry(
								StringEscapeUtils.escapeXml(
										String.format(
												"%sbrief-doc.html?query=PROVIDER:\"%s\"&view=table", 
												baseUrl, 
												URLEncoder.encode(facetField.getName(), "UTF-8"))),
								facetField.getName(),
								new Date(), //todo: add more relevant date later
								facetField.getCount()));
			} catch (UnsupportedEncodingException e) {
				Log.warn(e.getMessage() + " on " + facetField.getName());
			} 
		}
		mavPage = ControllerUtil.createModelAndViewPage("sitemap-index.html");
		mavPage.addObject("entries", entries);

		clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.SITE_MAP_XML, mavPage);
		return mavPage;
	}

	private List<FacetField.Count> getCollections () throws EuropeanaQueryException {
		SolrQuery solrQuery = new SolrQuery()
		.setQuery("*:*")
		.setRows(0)
		.setFacet(true)
        .addFacetField("PROVIDER")
		.setFacetMinCount(1)
		.setQueryType(QueryType.ADVANCED_QUERY.toString());
		final QueryResponse response = beanQueryModelFactory.getSolrResponse(solrQuery);
		List<FacetField.Count> facetFieldCount = null;
		for (FacetField facetField : response.getFacetFields()) {
			if (facetField.getName().equalsIgnoreCase("PROVIDER")) {
				facetFieldCount = facetField.getValues();
			}
		}
		if (facetFieldCount == null || facetFieldCount.size() == 0) {
			facetFieldCount = new ArrayList<FacetField.Count>();
		}
		return facetFieldCount;
	}

	/* *
	 * This is a test method to determine the load crawl bots put on the system and to see how a test collection
	 * "tel-treasures" is discoverable via the web search engine
	 *
	 * @param collection
	 * @param page
	 * @param request
	 * @return ModelAndView
	 * @throws org.apache.solr.client.solrj.SolrServerException
	 *
	 * @throws eu.europeana.core.querymodel.query.EuropeanaQueryException
	 *
	 * /
	//@RequestMapping("/sitemap.xml")
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
									europeanaCollection.getCollectionLastModified(),
									0));
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
		clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.SITE_MAP_XML, mavPage);
		return mavPage;
	}
	 */
	/**
	 * Sitemap index entry, model for MVC.
	 */
	public static class SitemapIndexEntry {

		private String loc;
		private String name;
		private final Date lastmod;
		private long count;

		public String getLoc() {
			return loc;
		}

		public Date getLastmod() {
			return lastmod;
		}

		public long getCount() {
			return count;
		}

		public String getName() {
			return name;
		}

		public SitemapIndexEntry(String loc, String name, Date lastmod, long count) {
			this.loc = loc;
			this.lastmod = lastmod;
			this.count = count;
			this.name = name;
		}

	}

}
