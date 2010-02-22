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

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests running against an external system.
 * 
 * @author Borys Omelayenko
 */

@RunWith(JUnit4.class)
public class SitemapTest {

	@Test
	public void testSitemapIndex() throws IOException {

		URL indexUrl = new URL(IntegrationTests.getTestSystemUrl() + "sitemap.xml");
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
}