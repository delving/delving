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

package eu.europeana.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import eu.europeana.integration.IntegrationTests;
import eu.europeana.integration.TestClientFixture;

/**
 * @author Borys Omelayenko
 */
public class SearchTest {

	/**
	 * Bible should be foundable in all languages
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {

		for (TestClientFixture ptt : IntegrationTests.multiLingualSetup()) {

			//enter search term
			String queryText = "bible";
			HtmlPage searchResult = search(ptt, queryText);
			Assert.assertNotNull(searchResult);
			Assert.assertNotNull(searchResult.getWebResponse().getContentAsString());
			Assert.assertTrue(searchResult.getWebResponse().getContentAsString().contains(queryText));
		}
	}

	public static HtmlPage search(TestClientFixture ptt, String queryText) throws IOException {
		HtmlTextInput query = (HtmlTextInput) ptt.getPage().getElementById("query");
		query.setValueAttribute(queryText);

		HtmlSubmitInput submitQuery = (HtmlSubmitInput) ptt.getPage().getElementById("submit_search");
		HtmlPage searchResult = submitQuery.click();
		return searchResult;
	}

	/**
	 * Refined search. 
	 * @throws Exception
	 */
	@Test
	public void testRefinedSearch() throws Exception {
		
		// http://europeanalabs.eu/ticket/1017
		URL indexUrl = new URL(FrontendTestUtil.testPortalUrl() 
				+ "full-doc.html?query=europeana_uri%3A%22http%3A%2F%2Fwww.europeana.eu%2Fresolve%2Frecord%2F09407%2F9B5AAE061094331DA1800CA24A805B70F1024042%22&tab=&start=2&startPage=1&" 
				+ "uri=http://www.europeana.eu/resolve/record/09407/ADD143857A36068C18E76353ECFCFB2BE9FB0DC9&view=table&pageId=brd");
		InputStream indexStream =  indexUrl.openStream();

		try {
			// something in
			Assert.assertFalse(IOUtils.toString(new InputStreamReader(indexStream, "UTF-8")).contains("Exception"));
		} finally {
			indexStream.close();
		}

	}
}
