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

package eu.europeana.integration.production;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import eu.europeana.frontend.FrontendTestUtil;

/**
 * 
 * Checks that our portals are alive.
 * 
 * @author Borys Omelayenko
 */
public class PortalsTest {

	
	public final String[] urls = {
			
			// production 
			"http://portal1.europeana.sara.nl/portal/",
			"http://portal2.europeana.sara.nl/portal/",
			"http://portal3.europeana.sara.nl/portal/",
			"http://portal4.europeana.sara.nl/portal/",
			
			// test
			"http://test1.europeana.sara.nl/portal/",
			"http://test2.europeana.sara.nl/portal/",
			
	};
	
    @Test
    public void test() throws IOException {

    	WebClient webClient = FrontendTestUtil.createWebClient();

    	for (String url : urls) {
       
    		HtmlPage homePage = webClient.getPage(url);

            //enter search term
            HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
            query.setValueAttribute("viva");

            HtmlSubmitInput submitQuery = (HtmlSubmitInput) homePage.getElementById("submit_search");
            HtmlPage searchResult = submitQuery.click();
            Assert.assertNotNull("No search result at " + url, searchResult);
            String content = searchResult.getWebResponse().getContentAsString();
			Assert.assertNotNull("Empty content at " + url, content);
			Assert.assertFalse("Exception at " + url, content.contains("xception"));
		}
    }
}
