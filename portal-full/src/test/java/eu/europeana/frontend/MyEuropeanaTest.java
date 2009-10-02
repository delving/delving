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
import eu.europeana.PortalFullStarter;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;

/**
 * @author vitali
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml",
        "/database-application-context.xml"})
public class MyEuropeanaTest {

    private Server server;

    @Autowired
    private UserDao userDao;

    @Before
    public void start() throws Exception {
        server = new PortalFullStarter().startServer();
        User user = new User();
        user.setFirstName(FrontendTestUtil.FIRST_NAME);
        user.setLastName(FrontendTestUtil.LAST_NAME);
        user.setUserName(FrontendTestUtil.USERNAME);
        user.setPassword(FrontendTestUtil.PASSWORD);
        user.setEmail(FrontendTestUtil.EMAIL);
        user.setEnabled(true);
        user.setLastLogin(new Date());
        user.setNewsletter(true);
        user.setRegistrationDate(new Date());
        user.setRole(Role.ROLE_USER);
        userDao.addUser(user);
    }

    @After
    public void stop() throws Exception {
        server.stop();
        User user = userDao.fetchUserByEmail(FrontendTestUtil.EMAIL);
        userDao.removeUser(user);
    }

    @Test
    public void saveSearch() throws IOException {
        WebClient webClient = FrontendTestUtil.createWebClient();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

        HtmlPage homePage = webClient.getPage("http://localhost:8080/portal/");

        HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
        query.setValueAttribute("duke");

        HtmlSubmitInput submit = (HtmlSubmitInput) homePage.getElementById("submit_search");
        HtmlPage searchResultPage = submit.click();

        HtmlAnchor saveQuery = (HtmlAnchor) searchResultPage.getElementById("saveQuery");
        saveQuery.click();

        User user = userDao.fetchUserByEmail(FrontendTestUtil.EMAIL);
        Assert.assertEquals(1, user.getSavedSearches().size());
//        System.out.println( "+++ " + user.getSavedSearches().size() );
    }
}
