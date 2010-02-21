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
import eu.europeana.bootstrap.PortalFullStarter;
import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Token;
import eu.europeana.web.util.TokenService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Borys Omelayenko
 * @author Vitali Kiruta
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml"})
public class RegistrationTest {

	private static final String REMEMBER_ME_COOKIE_NAME = AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserDao userDao;

	@Test
	public void register() throws Exception {

		WebClient webClient = FrontendTestUtil.createWebClient();

		// 1.
		//go to myeuropeana.
		//use a separate server for registration request.
		Server regRequestServer = new PortalFullStarter().startServer(8082);
		try {
			HtmlPage loginPage = webClient.getPage("http://localhost:8082/portal/myeuropeana.html");

			//fill in and submit registration-request form
			HtmlTextInput registerEmail = (HtmlTextInput) loginPage.getElementById("register_email");
			registerEmail.setValueAttribute(FrontendTestUtil.EMAIL);

			HtmlSubmitInput register = (HtmlSubmitInput) loginPage.getElementById("register");
			HtmlPage successPage = register.click();

			//assert success message
			HtmlElement successMessage = successPage.getElementById("success");
			Assert.assertNotNull(successMessage);
		} finally {
			regRequestServer.stop();
		}

		// 2.
		//retrive newly generated registration token
		Token token = tokenService.getTokenByEmail(FrontendTestUtil.EMAIL);

		//follow registration url (from the new browser window)
		//and on a different server.
		Server registrationServer = new PortalFullStarter().startServer(8084);
		try {
			HtmlPage registerSuccessPage = completeRegistration(token, 8084);
			//assert successful registration message
			Assert.assertNotNull( registerSuccessPage.getElementById("register_success"));
		} finally {
			registrationServer.stop();

		}

		// 3.
		//Now we can login. Do it from yet another browser window
		//and on a different server
		webClient = FrontendTestUtil.createWebClient();
		HtmlPage loginSuccessPage = FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, FrontendTestUtil.PASSWORD);
		Assert.assertEquals(FrontendTestUtil.TEST_PORTAL_URL, loginSuccessPage.getWebResponse().getRequestUrl().toExternalForm());

		//token has to be removed now
		token = tokenService.getTokenByEmail(FrontendTestUtil.EMAIL);
		Assert.assertNull(token);

		//remove user and token
		userDao.removeUser( userDao.fetchUserByEmail(FrontendTestUtil.EMAIL) );
	}


	/**
	 * @return registration success page
	 */
	private HtmlPage completeRegistration(Token token, int port) throws Exception {
		WebClient webClient = FrontendTestUtil.createWebClient();

		HtmlPage registrationPage = webClient.getPage(formatUrl(FrontendTestUtil.TEST_PORTAL_URL + "register.html", token.getToken()));

		//fill in registration form
		HtmlTextInput email = (HtmlTextInput) registrationPage.getElementById("email");
		email.setValueAttribute( FrontendTestUtil.EMAIL );

		HtmlTextInput userName = (HtmlTextInput) registrationPage.getElementById("userName");
		userName.setValueAttribute( FrontendTestUtil.USERNAME );

		HtmlPasswordInput password = (HtmlPasswordInput) registrationPage.getElementById("password");
		password.setValueAttribute( FrontendTestUtil.PASSWORD );

		HtmlPasswordInput password2 = (HtmlPasswordInput) registrationPage.getElementById("password2");
		password2.setValueAttribute( FrontendTestUtil.PASSWORD );

		HtmlSubmitInput submit = (HtmlSubmitInput) registrationPage.getElementById("submit_registration");
		return submit.click();
	}

	//TODO duplication with TokenReplyEmailSender
	private String formatUrl(String url, String token) {
		return url + "?token=" + token;
	}
}
