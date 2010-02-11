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
import java.util.Date;

import org.junit.Test;
import org.mortbay.jetty.Server;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import eu.europeana.bootstrap.ContentLoader;
import eu.europeana.bootstrap.PortalFullStarter;
import eu.europeana.bootstrap.SolrStarter;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.User;

/**
 * @author Borys Omelayenko
 * @author Vitali Kiruta
 */
public class FrontendTestUtil {

    public static class Constants {

		static final String CAROUSEL_EL_TYPE = "input";
		static final String CAROUSEL_STYLE = "carousel-input";
		static final String PACTA_STYLE = "pacta-input";
		final static String USER_1 = "1" + FrontendTestUtil.EMAIL;
		final static String USER_2 = "2" + FrontendTestUtil.EMAIL;
		final static String USER_SIMPLE = "simple" + FrontendTestUtil.EMAIL;

	}

	public static final int TEST_PORT = 8081;
    public static final String TEST_PORTAL_URL = "http://localhost:" + TEST_PORT + "/portal/";


    public static final String EMAIL = "test@example.com";
    public static final String USERNAME = "test_user";
    public static final String PASSWORD = "test";
    public static final String FIRST_NAME = "First";
    public static final String LAST_NAME = "Last";

    public static WebClient createWebClient() {
        WebClient webClient = new WebClient();

        //A temprorary workaround to avoid javascript error caused by jQuery 1.3.1
        //htmlunit does not fully support jQuery 1.3.1 yet.
        webClient.setThrowExceptionOnScriptError(false);

        // TODO: remove it

        //webClient.setJavaScriptEnabled(false);

        return webClient;
    }

    /*
     * get a Successful login page.
     */
    public static HtmlPage login(WebClient webClient, String username, String password) throws IOException {
        HtmlPage page = webClient.getPage(TEST_PORTAL_URL + "login.html"); //go to login page

        HtmlTextInput usernameInput = (HtmlTextInput) page.getElementById("j_username");
        usernameInput.setValueAttribute(username);

        HtmlPasswordInput passwordInput = (HtmlPasswordInput) page.getElementById("j_password");
        passwordInput.setValueAttribute(password);

        HtmlSubmitInput loginButton = (HtmlSubmitInput) page.getElementsByName("submit_login").get(0);
        return loginButton.click();
    }

	public static User createUser(String email, Role role) {
		User user = new User();
		user.setFirstName(FrontendTestUtil.FIRST_NAME);
		user.setLastName(FrontendTestUtil.LAST_NAME);
		user.setUserName(FrontendTestUtil.USERNAME);
		user.setPassword(FrontendTestUtil.PASSWORD);
		user.setEmail(email);
		user.setEnabled(true);
		user.setLastLogin(new Date());
		user.setNewsletter(true);
		user.setRegistrationDate(new Date());
		user.setRole(role);
		return user;
	}

	private static boolean loaded = false;
	private static Server server;
	private static SolrStarter solr;

	@Test
	public static void start() throws Exception {
		if (server == null) {
			PortalFullStarter starter = new PortalFullStarter();
			if (!loaded) {
				ContentLoader.main();
/*				LoadContent loader = new LoadContent() {

					@Override
					public void postLoad() {
						// create default users
						userDao.addUser(FrontendTestUtil.createUser(Constants.USER_1, Role.ROLE_EDITOR));
						userDao.addUser(FrontendTestUtil.createUser(Constants.USER_2, Role.ROLE_EDITOR));
						userDao.addUser(FrontendTestUtil.createUser(Constants.USER_SIMPLE, Role.ROLE_USER));
						userDao.addUser(FrontendTestUtil.createUser(EMAIL, Role.ROLE_USER));
					}

				};
				loader.init();
				loader.load(true);
				staticUserDao = loader.getUserDao();*/
				//LoadContent.main();//load(true, normalizedEseImporter, dashboardDao, normalizedImportRepository, languageDao, staticInfoDao);
				loaded = true;
			}
			server = starter.startServer(FrontendTestUtil.TEST_PORT);
			if (!server.isRunning())
				throw new Exception("Server not started");
			solr = new SolrStarter();
		}
		server.start();
		solr.start();
	}

	public static UserDao staticUserDao;

	@Deprecated
	public static UserDao getUserDao() {
		return staticUserDao;
	}

	//	@AfterClass
	public static void stop() throws Exception {
		server.stop();
		solr.stop();
	}


}
