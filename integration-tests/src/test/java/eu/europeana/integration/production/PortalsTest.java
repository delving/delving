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

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import eu.europeana.integration.IntegrationTests;
import eu.europeana.integration.TestClientFixture;

/**
 * 
 * Checks that our portals are alive.
 * 
 * @author Borys Omelayenko
 */
public class PortalsTest {

		
    @Test
    public void test() throws Exception {

    	for (TestClientFixture page : IntegrationTests.multiBrowserSetup()) {
       
            //enter search term
            HtmlTextInput query = (HtmlTextInput) page.getPage().getElementById("query");
            HtmlPage p = (HtmlPage) query.setValueAttribute("bible");

            HtmlSubmitInput submitQuery = (HtmlSubmitInput) p.getElementById("submit_search");
            HtmlPage searchResult = submitQuery.click();
            Assert.assertNotNull("No search result at " + page, searchResult);
            String content = searchResult.getWebResponse().getContentAsString();
			Assert.assertNotNull("Empty content at " + page, content);
			Assert.assertFalse("Exception at " + page, content.contains("xception"));
			Assert.assertTrue("On " + page, content.contains("ible"));
			IntegrationTests.assertText(searchResult, "//table[@id='multi']/tbody/tr[1]/td[2]/h2", "ible");
		}
    }
}
