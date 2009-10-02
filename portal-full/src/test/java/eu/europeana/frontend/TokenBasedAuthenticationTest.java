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
import eu.europeana.PortalFullStarter;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.User;
import org.apache.commons.httpclient.Cookie;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.rememberme.AbstractRememberMeServices;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;

/**
 * @author Vitali Kiruta
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml",
        "/database-application-context.xml"})
public class TokenBasedAuthenticationTest {

    private static final String REMEMBER_ME_COOKIE_NAME = AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
    private org.mortbay.jetty.Server server;

    @Autowired
    private UserDao userDao;

    @Before
    public void start() throws Exception {
        server = new PortalFullStarter().startServer();
        User user = new User();
        user.setEmail(FrontendTestUtil.EMAIL);
        user.setPassword(FrontendTestUtil.PASSWORD);
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        user.setRegistrationDate(new Date());
        user.setUserName(FrontendTestUtil.EMAIL);
        userDao.addUser(user);
    }

    @After
    public void stop() throws Exception {
        server.stop();
        User user = userDao.fetchUserByEmail(FrontendTestUtil.EMAIL);
        userDao.removeUser(user);
    }

    @Test
    public void token() throws IOException {
        WebClient webClient = FrontendTestUtil.createWebClient();

        //login
        HtmlPage successfulLoginPage = FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);
        Assert.assertEquals("Europeana - Homepage", successfulLoginPage.getTitleText());

        Cookie rememberMeCookie = webClient.getCookieManager().getCookie(REMEMBER_ME_COOKIE_NAME);
        Assert.assertNotNull(rememberMeCookie);

        HtmlPage myEuropeanaPage = webClient.getPage("http://localhost:8080/portal/myeuropeana.html"); //go to secured page
        Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());


        //Create new web client and steal cookie
        WebClient thiefClient = FrontendTestUtil.createWebClient();

        rememberMeCookie = webClient.getCookieManager().getCookie(REMEMBER_ME_COOKIE_NAME);
        thiefClient.getCookieManager().addCookie(rememberMeCookie);

        HtmlPage stolenPage = thiefClient.getPage("http://localhost:8080/portal/myeuropeana.html");
        Assert.assertEquals("Europeana - My Europeana", stolenPage.getTitleText());

        //Now try to access the original "session"
        webClient.setThrowExceptionOnFailingStatusCode(false);
        myEuropeanaPage = webClient.getPage("http://localhost:8080/portal/myeuropeana.html"); //go to secured page
        Assert.assertEquals(200, myEuropeanaPage.getWebResponse().getStatusCode());
        Assert.assertEquals("http://localhost:8080/portal/login.html", myEuropeanaPage.getWebResponse().getRequestUrl().toExternalForm()); //user was logged out and is now redirected to Login page.

        //Now access thief "session" again.
        stolenPage = thiefClient.getPage("http://localhost:8080/portal/myeuropeana.html");
        Assert.assertEquals(200, stolenPage.getWebResponse().getStatusCode());
        Assert.assertEquals("http://localhost:8080/portal/login.html", stolenPage.getWebResponse().getRequestUrl().toExternalForm()); //user was logged out and is now redirected to Login page.
    }

    @Test
    public void tokenKeptAfterRestart() throws Exception {
        WebClient webClient = FrontendTestUtil.createWebClient();
        FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

        //access secure page
        HtmlPage myEuropeanaPage = webClient.getPage("http://localhost:8080/portal/myeuropeana.html");
        Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());

        //restart server
        server.stop();
        server.start();

        //access secure page again
        myEuropeanaPage = webClient.getPage("http://localhost:8080/portal/myeuropeana.html");
        Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());
    }

    @Test
    public void loadBalancing() throws Exception {
        //Amulate load balancing by starting two different servers and
        //connecting to each one sequentially
        WebClient webClient = FrontendTestUtil.createWebClient();
        FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

        //access secure page
        HtmlPage myEuropeanaPage = webClient.getPage("http://localhost:8080/portal/myeuropeana.html");
        Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());

        org.mortbay.jetty.Server server2 = new PortalFullStarter().startServer(8082);

        //access secure page on the second page. Should still be logged in
        myEuropeanaPage = webClient.getPage("http://localhost:8082/portal/myeuropeana.html");
        Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());

    }

}
