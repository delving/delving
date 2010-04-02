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

}
