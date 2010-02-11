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

package eu.europeana.integration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Integration tests running against an external system.
 * 
 * @author Borys Omelayenko
 */

@RunWith(JUnit4.class)
public class FrontpageTest {

	public static HtmlPage navigateSearchSelect(String queryString, boolean goToFullView) throws IOException {
		HtmlPage page = IntegrationTests.getPortalPage();
		// search for bible
		HtmlPage query = (HtmlPage)((HtmlTextInput) page.getElementById("query")).setValueAttribute(queryString);
		HtmlSubmitInput submit = (HtmlSubmitInput) query.getElementById("submit_search");
		HtmlPage searchResultPage = submit.click();

		if (goToFullView) {
			// select the first bible, go to full view
			HtmlImage selectBibleImg = (HtmlImage) searchResultPage.getElementById("thumb_1");
			HtmlAnchor selectBible = (HtmlAnchor) selectBibleImg.getParentNode();
			return selectBible.click();
		}

		return searchResultPage;
	}

	/**
	 * Checks translations of the front page text "This is Europeana ...".
	 * @throws IOException
	 */
	@Test
	public void testLanguage() throws IOException {
		HtmlPage page = IntegrationTests.getPortalPage();
		Assert.assertTrue(IntegrationTests.assertText(page, "//div[@id='top']/h1", "This is Europeana - a place for inspiration and ideas"));
		HtmlSelect inputByName = page.getElementByName("dd_lang");
		page = (HtmlPage)inputByName.setSelectedAttribute("nl", true);
		Assert.assertTrue(IntegrationTests.assertText(page, "//div[@id='top']/h1", "Dit is Europeana - een plaats voor inspiratie en ide"));
	}

	/**
	 * Checks that we can find a bible.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testBible() throws IOException {
		HtmlPage page = navigateSearchSelect("bible", false);
		Assert.assertTrue(IntegrationTests.assertText(page, "//table[@id='multi']/tbody/tr[1]/td[2]/h2", "ible"));
	}

	/**
	 * Checks that there are at least 10 carousel items, where there are a few visible and with images.
	 * @throws IOException
	 */
	@Test
	public void testCarousel() throws IOException {
		HtmlPage page = IntegrationTests.getPortalPage();

		// check if carousel is present
		HtmlElement carousel = page.getElementById("mycarousel");
		Assert.assertNotNull(carousel);

		// check if it has at least 10 items
		List<HtmlElement> carouselItems = carousel.getHtmlElementsByTagName("li");
		Assert.assertTrue(carouselItems.size() > 10);

		// check that there are real items with images that are BEING SHOWN
		int shown = 0;
		for (HtmlElement carouselItem : carouselItems) {
			Assert.assertTrue(carouselItem.getAttribute("class").contains("jcarousel-item jcarousel-item-horizontal jcarousel-item-"));

			if (carouselItem.hasChildNodes()) {
				shown ++;
				// link to image
				HtmlAnchor a = (HtmlAnchor)carouselItem.getElementsByTagName("a").get(0);
				Assert.assertNotNull(a);

				// image
				HtmlImage img = (HtmlImage)a.getElementsByTagName("img").get(0);
				Assert.assertNotNull(img);

				BufferedImage image = ImageIO.read(new URL(img.getSrcAttribute()));
				Assert.assertNotNull(image);
				Assert.assertTrue(image.getHeight() > 0);
				Assert.assertTrue(image.getWidth() > 0);
			}
		}

		Assert.assertTrue(shown > 5);

		
	}
}