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

import org.apache.commons.httpclient.Cookie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import eu.europeana.bootstrap.PortalFullStarter;

/**
 * @author Borys Omelayenko
 * @author Vitali Kiruta
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml"})
public class TokenBasedAuthenticationTest {

	private static final String MYEUROPEANA_URL = FrontendTestUtil.TEST_PORTAL_URL + "myeuropeana.html";
	private static final String REMEMBER_ME_COOKIE_NAME = AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

	@Test
	public void token() throws IOException {
		WebClient webClient = FrontendTestUtil.createWebClient();

		//login
		HtmlPage successfulLoginPage = FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);
		Assert.assertEquals("Europeana - Homepage", successfulLoginPage.getTitleText());

		Cookie rememberMeCookie = webClient.getCookieManager().getCookie(REMEMBER_ME_COOKIE_NAME);
		Assert.assertNotNull(rememberMeCookie);

		HtmlPage myEuropeanaPage = webClient.getPage(MYEUROPEANA_URL); //go to secured page
		Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());


		//Create new web client and steal cookie
		WebClient thiefClient = FrontendTestUtil.createWebClient();

		rememberMeCookie = webClient.getCookieManager().getCookie(REMEMBER_ME_COOKIE_NAME);
		thiefClient.getCookieManager().addCookie(rememberMeCookie);

		HtmlPage stolenPage = thiefClient.getPage(MYEUROPEANA_URL);
		Assert.assertEquals("Europeana - My Europeana", stolenPage.getTitleText());

		//Now try to access the original "session"
		webClient.setThrowExceptionOnFailingStatusCode(false);
		myEuropeanaPage = webClient.getPage(MYEUROPEANA_URL); //go to secured page
		Assert.assertEquals(200, myEuropeanaPage.getWebResponse().getStatusCode());
		Assert.assertEquals(FrontendTestUtil.TEST_PORTAL_URL + "login.html", myEuropeanaPage.getWebResponse().getRequestUrl().toExternalForm()); //user was logged out and is now redirected to Login page.

		//Now access thief "session" again.
		stolenPage = thiefClient.getPage(MYEUROPEANA_URL);
		Assert.assertEquals(200, stolenPage.getWebResponse().getStatusCode());
		Assert.assertEquals(FrontendTestUtil.TEST_PORTAL_URL + "login.html", stolenPage.getWebResponse().getRequestUrl().toExternalForm()); //user was logged out and is now redirected to Login page.
	}

	@Test
	public void tokenKeptAfterRestart() throws Exception {
		WebClient webClient = FrontendTestUtil.createWebClient();
		FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

		//access secure page
		HtmlPage myEuropeanaPage = webClient.getPage(MYEUROPEANA_URL);
		Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());

		//restart server
		FrontendTestUtil.stop();
		FrontendTestUtil.start();

		//access secure page again
		myEuropeanaPage = webClient.getPage(MYEUROPEANA_URL);
		Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());
	}

	@Test
	public void loadBalancing() throws Exception {
		//Amulate load balancing by starting two different servers and
		//connecting to each one sequentially
		WebClient webClient = FrontendTestUtil.createWebClient();
		FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);

		//access secure page
		HtmlPage myEuropeanaPage = webClient.getPage(MYEUROPEANA_URL);
		Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());

		Server server = new PortalFullStarter().startServer(8082);
		try {
			//access secure page on the second page. Should still be logged in
			myEuropeanaPage = webClient.getPage("http://localhost:8082/portal/myeuropeana.html");
			Assert.assertEquals("Europeana - My Europeana", myEuropeanaPage.getTitleText());
		} finally {
			server.stop();
		}
	}

}
