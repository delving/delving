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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import eu.europeana.PortalFull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @author vitali
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml",
        "/database-application-context.xml"})
public class SearchTest {

    private Server server;
//    private Server solrServer;

    @Before
    public void before() throws Exception {
        server = new PortalFull().startServer();
//        solrServer = new SolrStarter().start();
    }

    @After
    public void after() throws Exception {
//        solrServer.stop();
        server.stop();
    }

    @Test
    public void test() throws IOException {
        WebClient webClient = FrontendTestUtil.createWebClient();

        HtmlPage homePage = webClient.getPage("http://localhost:8080/portal/");

        //enter search term
        HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
        query.setValueAttribute("viva");

        HtmlSubmitInput submitQuery = (HtmlSubmitInput) homePage.getElementById("submit_search");
        HtmlPage searchResult = submitQuery.click();

        System.out.println( searchResult.getWebResponse().getContentAsString() );
    }
}
