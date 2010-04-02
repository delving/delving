/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import eu.europeana.frontend.FrontendTestUtil;
import eu.europeana.frontend.SearchTest;

/**
 * Integration tests running against an external system.
 * 
 * @author Borys Omelayenko
 */

@RunWith(JUnit4.class)
public class SitemapTest {

	@Test
	public void testSitemapIndex() throws IOException {

		URL indexUrl = new URL(FrontendTestUtil.testPortalUrl() + "europeana-sitemap.xml");
		InputStream indexStream =  indexUrl.openStream();

		// iterate links to ensure that they are alive
		int linkCount = 0;
		try {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("<loc>") && line.endsWith("</loc>")) {

					String sitemapUrl = StringUtils.stripStart(line, "<loc>");
					sitemapUrl = StringUtils.stripEnd(sitemapUrl, "</loc>");
					sitemapUrl = sitemapUrl.replace("&amp;", "&");
					String sitemap = IntegrationTests.getUrlAsString(sitemapUrl);		

					// check if it exists
					Assert.assertFalse(sitemap.isEmpty());	

					// check if not empty
					Assert.assertTrue(sitemap.contains("<loc>"));	

					linkCount ++;

					// we just test first few pages, as the rest is too long
					if (linkCount > 50) 
						return;
				}
			}
		} finally {
			indexStream.close();
		}

		// something in
		Assert.assertTrue("No sitemap pages!", linkCount > 3);
	}

	@Test
	public void testBuildNumber() throws IOException {

		URL indexUrl = new URL(FrontendTestUtil.testPortalUrl() + "build.txt");
		InputStream indexStream =  indexUrl.openStream();

		// iterate links to ensure that they are alive
		try {
			// something in
			Assert.assertTrue("No build.txt",  IOUtils.readLines(new InputStreamReader(indexStream, "UTF-8")).size() > 0);
		} finally {
			indexStream.close();
		}

	}

	@Test
	public void testMetaTags() throws Exception {

		for (TestClientFixture ptt : IntegrationTests.singleSetup()) {
			try {
				// not going religious - simply choosing a two-words combination that is in all our servers
				String queryText = "Iesus Christ";
				HtmlPage page = SearchTest.search(ptt, queryText);

				// meta robots
				HtmlElement robots = page.getElementsByTagName("meta").get(1);
				Assert.assertEquals("robots", robots.getAttribute("name"));
				Assert.assertEquals("noindex,nofollow", robots.getAttribute("content"));

				// meta title
				HtmlElement title = page.getElementsByTagName("title").get(0);
				Assert.assertEquals(queryText, title.getTextContent());

				// go to full view
				for(HtmlAnchor a : page.getAnchors()) {
					// a little bit straightforward way
					if (a.getHrefAttribute().contains("F0ED0D")) {

						HtmlPage fullView = a.click();

						// meta description
						HtmlElement description = fullView.getElementsByTagName("meta").get(1);
						Assert.assertEquals("La vie de Iesus Christ", description.getAttribute("content"));

						// meta robots
						robots = fullView.getElementsByTagName("meta").get(2);
						Assert.assertEquals("robots", robots.getAttribute("name"));
						Assert.assertEquals("nofollow", robots.getAttribute("content"));

						// meta title
						title = fullView.getElementsByTagName("title").get(0);
						Assert.assertEquals("La vie de Iesus Christ", title.getTextContent());


					}
				}
			} catch (Exception e) {
				throw new Exception("On " + ptt, e);
			}
		}
	}
}