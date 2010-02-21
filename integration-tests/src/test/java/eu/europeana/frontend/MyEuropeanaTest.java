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

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import eu.europeana.bootstrap.PortalFullStarter;
import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @author Borys Omelayenko
 * @author vitali
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-application-context.xml","classpath:core-application-context.xml"})
public class MyEuropeanaTest {

	@Autowired
	UserDao userDao;

	@Test
    public void saveSearch() throws IOException {

        WebClient webClient = FrontendTestUtil.createWebClient();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

        HtmlPage homePage = webClient.getPage(PortalFullStarter.PORTAL_URL);

        HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
        query.setValueAttribute("duke");

        HtmlSubmitInput submit = (HtmlSubmitInput) homePage.getElementById("submit_search");
        HtmlPage searchResultPage = submit.click();

        HtmlAnchor saveQuery = (HtmlAnchor) searchResultPage.getElementById("saveQuery");
        saveQuery.click();

        // TODO: move to DAO tests
        User user = userDao.fetchUserByEmail(FrontendTestUtil.EMAIL);
        Assert.assertEquals(1, user.getSavedSearches().size());
//        System.out.println( "+++ " + user.getSavedSearches().size() );
    }
}
