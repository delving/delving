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
import com.gargoylesoftware.htmlunit.html.*;
import eu.europeana.PortalFull;
import eu.europeana.database.UserDao;
import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.User;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Borys Omelayenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml",
"/database-application-context.xml"})
public class CarouselTest {

	private static final String CAROUSEL_EL_TYPE = "input";
	private static final String CAROUSEL_STYLE = "carousel-input";

	private static final String PACTA_STYLE = "pacta-input";

	private final String USER_1 = "1" + FrontendTestUtil.EMAIL;
	private final String USER_2 = "2" + FrontendTestUtil.EMAIL;
	private final String USER_SIMPLE = "simple" + FrontendTestUtil.EMAIL;

	private Server server;

	@Autowired
	private UserDao userDao;

	//    @Autowired
	//    private CarouselItemDao carouselDao;

	@Autowired
	private DashboardDao dashboardDao;
	private WebClient webClient;

	@Before
	public void start() throws Exception {
		PortalFull starter = new PortalFull();
		System.out.println(System.getProperty("user.dir"));
		server = starter.startServer(FrontendTestUtil.TEST_PORT);
		if (!server.isRunning())
			throw new Exception("Server not started");

		User oldUser = userDao.fetchUserByEmail(FrontendTestUtil.EMAIL);
		if (oldUser != null)
			userDao.removeUser(oldUser);
		createUser(USER_1, Role.ROLE_EDITOR);
		createUser(USER_2, Role.ROLE_EDITOR);
		createUser(USER_SIMPLE, Role.ROLE_USER);
	}

	private void createUser(String email, Role role) {
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
		userDao.addUser(user);
	}

	@After
	public void stop() throws Exception {
		server.stop();
		removeUser(USER_1);
		removeUser(USER_2);
		removeUser(USER_SIMPLE);
	}

	private void removeUser(String email) {
		User user = userDao.fetchUserByEmail(email);
		userDao.removeUser(user);
	}

	public HtmlPage navigateSearchSelect(String email, String queryString, boolean goToFullView) throws IOException {
		webClient = FrontendTestUtil.createWebClient();
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());

		// login
		FrontendTestUtil.login(webClient, email, FrontendTestUtil.PASSWORD);

		// search for bible
		HtmlPage homePage = webClient.getPage(FrontendTestUtil.TEST_PORTAL_URL);
		HtmlTextInput query = (HtmlTextInput) homePage.getElementById("query");
		query.setValueAttribute(queryString);
		HtmlSubmitInput submit = (HtmlSubmitInput) homePage.getElementById("submit_search");
		HtmlPage searchResultPage = submit.click();

		if (goToFullView) {
			// select the first bible, go to full view
			HtmlImage selectBibleImg = (HtmlImage) searchResultPage.getElementById("thumb_1");
			HtmlAnchor selectBible = (HtmlAnchor) selectBibleImg.getParentNode();
			return selectBible.click();
		}

		return searchResultPage;
	}

	public Element getFirstElementOfClass(NodeList elements, String htmlClassName)  {
		for (int i = 0; i < elements.getLength(); i++)
		{
			Element el = (Element) elements.item(i);
			if (htmlClassName.equals(el.getAttribute("class")))
				return el;
		}
		return null;
	}

	@Test
	public void saveCarousel() throws IOException {
		/*
        User (editor) adding a carousel item
		 */
		HtmlInput inputElement;
		HtmlPage savedItemsPage = createTestFixtureOne(
				"bible", 
				"saveToMyEuropeana", 
				"savedItemsCount", 
				CAROUSEL_STYLE,
				true);
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE),
				CAROUSEL_STYLE);
		// check fixture at front end
		Assert.assertNotNull(inputElement);
		Assert.assertFalse(inputElement.isChecked());
		Assert.assertFalse("lock", inputElement.isDisabled());

		// check fixture at back end
		User user_1 = userDao.fetchUserByEmail(USER_1);
		List<SavedItem> si = user_1.getSavedItems();
		Assert.assertEquals(1, si.size() );
		Assert.assertNull(si.get(0).getCarouselItem());

		// do test: add a carousel item

		inputElement.click();

		// assess result at back end
		checkFirst_SavedItem_HasCheckedEnabledCarousel(savedItemsPage, USER_1);

		/*
          Another user adding another item
		 */
		{
			// create test fixture  via front end
			HtmlPage fullViewPageUser2 = navigateSearchSelect(USER_2, "bible", true);
			// add to carousel
			HtmlAnchor carouselBible = (HtmlAnchor) fullViewPageUser2.getElementById("saveToMyEuropeana");
			carouselBible.click();

			// check fixture at back end
			User user_2 = userDao.fetchUserByEmail(USER_2);
			List<SavedItem> si3 = user_2.getSavedItems();
			Assert.assertEquals(1, si3.size() );
			Assert.assertNull(si3.get(0).getCarouselItem());

			// fixture at front end: list with saved items
			// it should be in-carousel and disabled
			HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPageUser2.getElementById("savedItemsCount");
			HtmlPage savedItemsPage2 = savedItemsA.click();

			Assert.assertNotNull(
					getFirstElementOfClass(
							savedItemsPage2.getElementsByTagName("button"),
					"del-button"));
			HtmlInput inputEl = (HtmlInput) getFirstElementOfClass(
					savedItemsPage2.getElementsByTagName(CAROUSEL_EL_TYPE), CAROUSEL_STYLE);



			// check fixture at front end
			// should be already checked and disabled for this second user to edit
			Assert.assertNotNull(inputEl);
			Assert.assertTrue(inputEl.isChecked());
			Assert.assertTrue("lock 2", inputEl.isDisabled());
		}

		/*
        Navigate out and in
		 */
		HtmlPage fullViewPageAgain = navigateSearchSelect(USER_1, "bible", true);

		// go to saved items
		HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPageAgain.getElementById("savedItemsCount");
		savedItemsPage = savedItemsA.click();
		Assert.assertNotNull(
				getFirstElementOfClass(
						savedItemsPage.getElementsByTagName("button"),"del-button"));
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), CAROUSEL_STYLE);
		// check that is still in carousel
		checkFirst_SavedItem_HasCheckedEnabledCarousel(savedItemsPage, USER_1);

		// remove saved item
		inputElement.click();

		// backend - saved item is no longer in carousel
		User user_1bis = userDao.fetchUserByEmail(USER_1);
		List<SavedItem> si2 = dashboardDao.fetchSavedItems(user_1bis.getId());
		Assert.assertEquals(1, si2.size() );
		Assert.assertNull(si2.get(0).getCarouselItem());

		// frontend
		savedItemsPage.refresh();
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), CAROUSEL_STYLE);
		Assert.assertNotNull(inputElement);
		Assert.assertFalse(inputElement.isChecked());
		Assert.assertFalse("lock", inputElement.isDisabled());
	}

	private void checkFirst_SavedItem_HasCheckedEnabledCarousel(HtmlPage savedItemsPage, String user) {
		User user_1 = userDao.fetchUserByEmail(user);
		List<SavedItem> si2 = dashboardDao.fetchSavedItems(user_1.getId());
		Assert.assertEquals(1, si2.size() );
		Assert.assertNotNull("Expected in carousel", si2.get(0).getCarouselItem());

		// assess result at front end
		HtmlInput inputElement2 = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), CAROUSEL_STYLE);
		Assert.assertNotNull(inputElement2);
		Assert.assertTrue(inputElement2.isChecked());
		Assert.assertFalse(inputElement2.isDisabled());		
	}

	private void checkFirst_SavedSearch_HasCheckedEnabledEditorPick(HtmlPage savedItemsPage, String user) {
		User user_1 = userDao.fetchUserByEmail(user);
		List<SavedSearch> si2 = dashboardDao.fetchSavedSearches(user_1.getId());
		Assert.assertEquals(1, si2.size() );
		Assert.assertNotNull("Expected in pacta", si2.get(0).getEditorPick());

		// assess result at front end
		HtmlInput inputElement2 = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), PACTA_STYLE);
		Assert.assertNotNull(inputElement2);
		Assert.assertTrue(inputElement2.isChecked());
		Assert.assertFalse(inputElement2.isDisabled());
	}

	@Test
	public void saveCarouselNonEditor() throws IOException {
		/*
        User (editor) adding a carousel item
		 */
		HtmlInput inputElement;
		HtmlPage savedItemsPage;
		// create test fixture  via front end
		HtmlPage fullViewPage = navigateSearchSelect(USER_SIMPLE, "bible", true);
		// add to carousel
		HtmlAnchor carouselBible = (HtmlAnchor) fullViewPage.getElementById("saveToMyEuropeana");
		carouselBible.click();

		HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPage.getElementById("savedItemsCount");
		savedItemsPage = savedItemsA.click();
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE),
				CAROUSEL_STYLE);
		Assert.assertNull(inputElement);
		// earlier i believed we should get disabled checkbox - Borys 09.07.28		
		//		Assert.assertNotNull("Expected to find (disabled) checkbox", inputElement);
		//		Assert.assertTrue("Expected to find _disabled_ checkbox", inputElement.isDisabled());
	}


	private HtmlPage createTestFixtureOne(
			String queryString, 
			String saveElementId, 
			String countElementId,
			String checkboxStyle,
			boolean showFullView) 
	throws IOException {

		HtmlInput inputElement;
		HtmlPage savedItemsPage;

		HtmlPage fullViewPage = navigateSearchSelect(USER_1, queryString, showFullView);

		// save query
		((HtmlAnchor) fullViewPage.getElementById(saveElementId)).click();

		// check no pacta items are there
		savedItemsPage = ((HtmlAnchor) fullViewPage.getElementById(countElementId)).click();
		System.out.println(savedItemsPage.asXml());
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), checkboxStyle);
		Assert.assertNotNull(inputElement);
		Assert.assertFalse(inputElement.isChecked());
		Assert.assertFalse("lock", inputElement.isDisabled());

		HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPage.getElementById(countElementId);
		savedItemsPage = savedItemsA.click();
		Assert.assertNotNull(
				getFirstElementOfClass(savedItemsPage.getElementsByTagName("button"), "del-button"));

		return savedItemsPage;
	}

	@Test
	public void savePACTA() throws IOException {
		/*
        User (editor) adding a query
		 */
		HtmlInput inputElement;
		HtmlPage savedItemsPage = createTestFixtureOne(
				"calendar", 
				"saveQuery", 
				"savedSearchesCount",
				PACTA_STYLE,
				false);

		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), PACTA_STYLE);

		// check fixture at front end
		Assert.assertNotNull(inputElement);
		Assert.assertFalse(inputElement.isChecked());
		Assert.assertFalse("lock", inputElement.isDisabled());

		// check fixture at back end
		User user_1 = userDao.fetchUserByEmail(USER_1);
		List<SavedSearch> si = user_1.getSavedSearches();
		Assert.assertEquals(1, si.size() );
		Assert.assertNull(si.get(0).getEditorPick());

		// do test: add an editor pick
		inputElement.click();

		// assess result at back end
		checkFirst_SavedSearch_HasCheckedEnabledEditorPick(savedItemsPage, USER_1);

		// test would break somewhere here
		// TODO: check and reuse code with carousel test
		/*
          Another user adding another item
		 */
		{
			// create test fixture  via front end
			HtmlPage fullViewPageUser2 = navigateSearchSelect(USER_2, "calendar", true);
			// add to carousel
			HtmlAnchor carouselBible = (HtmlAnchor) fullViewPageUser2.getElementById("saveQuery");
			carouselBible.click();

			// check fixture at back end
			User user_2 = userDao.fetchUserByEmail(USER_2);
			List<SavedSearch> si3 = user_2.getSavedSearches();
			Assert.assertEquals(1, si3.size() );
			Assert.assertNull(si3.get(0).getEditorPick());

			// fixture at front end: list with saved items
			// it should be in-carousel and disabled
			HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPageUser2.getElementById("savedSearchesCount");
			HtmlPage savedItemsPage2 = savedItemsA.click();

			Assert.assertNotNull(
					getFirstElementOfClass(
							savedItemsPage2.getElementsByTagName("button"),
					"del-button"));
			HtmlInput inputEl = (HtmlInput) getFirstElementOfClass(
					savedItemsPage2.getElementsByTagName(CAROUSEL_EL_TYPE), PACTA_STYLE);



			// check fixture at front end
			// should be already checked and disabled for this second user to edit
			Assert.assertNotNull(inputEl);
			Assert.assertTrue(inputEl.isChecked());
			Assert.assertTrue("lock 2", inputEl.isDisabled());
		}

		/*
        Navigate out and in
		 */
		HtmlPage fullViewPageAgain = navigateSearchSelect(USER_1, "calendar", true);

		// go to saved items
		HtmlAnchor savedItemsA = (HtmlAnchor) fullViewPageAgain.getElementById("savedSearchesCount");
		savedItemsPage = savedItemsA.click();
		Assert.assertNotNull(
				getFirstElementOfClass(
						savedItemsPage.getElementsByTagName("button"),"del-button"));
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), PACTA_STYLE);
		// check that is still in carousel
		checkFirst_SavedSearch_HasCheckedEnabledEditorPick(savedItemsPage, USER_1);

		// remove saved item
		inputElement.click();

		// backend - saved item is no longer in carousel
		User user_1bis = userDao.fetchUserByEmail(USER_1);
		List<SavedSearch> si2 = dashboardDao.fetchSavedSearches(user_1bis.getId());
		Assert.assertEquals(1, si2.size() );
		Assert.assertNull(si2.get(0).getEditorPick());

		// frontend
		savedItemsPage.refresh();
		inputElement = (HtmlInput) getFirstElementOfClass(
				savedItemsPage.getElementsByTagName(CAROUSEL_EL_TYPE), PACTA_STYLE);
		Assert.assertNotNull(inputElement);
		Assert.assertFalse(inputElement.isChecked());
		Assert.assertFalse("lock", inputElement.isDisabled());
	}

}