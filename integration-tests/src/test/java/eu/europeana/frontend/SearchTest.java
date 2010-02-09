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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * @author Borys Omelayenko
 * @author vitali
 */
public class SearchTest {

    @Test
    public void test() throws IOException {
        WebClient webClient = FrontendTestUtil.createWebClient();

        HtmlPage homePage = webClient.getPage(FrontendTestUtil.TEST_PORTAL_URL);

        //enter search term
        HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
        query.setValueAttribute("viva");

        HtmlSubmitInput submitQuery = (HtmlSubmitInput) homePage.getElementById("submit_search");
        HtmlPage searchResult = submitQuery.click();
        Assert.assertNotNull(searchResult);
        Assert.assertNotNull(searchResult.getWebResponse().getContentAsString());
        //System.out.println( searchResult.getWebResponse().getContentAsString() );
    }
}
