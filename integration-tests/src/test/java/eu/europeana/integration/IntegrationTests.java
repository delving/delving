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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.apache.commons.fileupload.util.Streams;
import org.junit.Ignore;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import eu.europeana.frontend.FrontendTestUtil;


/**
 * Returns all portal pages, for all languages, for all browsers.
 * 
 * @author Borys Omelayenko
 */
@Ignore
public class IntegrationTests implements Iterable<TestClientFixture> {


	private static BrowserVersion[] browsers = {
		BrowserVersion.FIREFOX_3,
		BrowserVersion.INTERNET_EXPLORER_7,
		BrowserVersion.INTERNET_EXPLORER_8
	};

	List<TestClientFixture> list = new ArrayList<TestClientFixture>();

	private static IntegrationTests singleSetup;
	public static IntegrationTests singleSetup() {
		if (singleSetup == null) {
			singleSetup = new IntegrationTests(false, false);
		}
		return singleSetup;
	}
	private static IntegrationTests multiBrowserSetup;
	public static IntegrationTests multiBrowserSetup() {
		if (multiBrowserSetup == null) {
			multiBrowserSetup = new IntegrationTests(false, true);
		}
		return multiBrowserSetup;		
	}
	private static IntegrationTests multiLingualSetup;
	public static IntegrationTests multiLingualSetup() {
		if (multiLingualSetup == null) {
			multiLingualSetup = new IntegrationTests(true, true);
		}
		return multiLingualSetup;		
	}

	private IntegrationTests(boolean multilingual, boolean multibrowser) {
		// list of pages
		for (BrowserVersion browser : browsers) {
			for (String url : FrontendTestUtil.portalUrls()) {
				// get page
				WebClient webClient = new WebClient(browser);
				webClient.setAjaxController(new NicelyResynchronizingAjaxController());
				webClient.setJavaScriptEnabled(true);


				HtmlPage page;
				try {
					page = webClient.getPage(url);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				// get languages
				List<String> languages = new ArrayList<String>();
				languages.add(null);

				List<HtmlElement> options = page.getElementByName("dd_lang").getElementsByTagName("option");
				for (HtmlElement option : options) {
					if (option.getAttribute("value").length() == 2) {
						languages.add(option.getAttribute("value"));
					}
				}

				// get pages
				for (String lang : languages) {
					list.add(new TestClientFixture(page, lang, url, browser));	
					
					// stop after first language
					if (!multilingual) {
						break;
					}
				}					
			}

			// stop after first browser
			if (!multibrowser) {
				break;
			}
		}

		if (list.size() > 30 * browsers.length * FrontendTestUtil.portalUrls().size()) {
			throw new RuntimeException("Too many languages: " + list.size());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<TestClientFixture> iterator() {
		return new AbstractIteratorDecorator(list.iterator()) {

			@Override
			public Object next() {
				TestClientFixture page = (TestClientFixture) super.next();
				HtmlSelect inputByName = page.getPage().getElementByName("dd_lang");
				HtmlPage langPage = page.getPage();
				if (page.getLang() != null) {
					langPage = (HtmlPage)inputByName.setSelectedAttribute(page.getLang(), true);
				}
				return new TestClientFixture(langPage, page.getLang(), page.getUrl(), page.getBrowser());
			}

		};
	}

	public static boolean assertText(HtmlPage page, String xpath, String text) {
		return ((HtmlElement)page.getByXPath(xpath).get(0)).asText().contains(text);

	}

	public static String getUrlAsString(String absoluteUrl) throws IOException {
		URL url = new URL(absoluteUrl);
		InputStream in = url.openStream();
		try {
			return Streams.asString(in);
		} finally {
			in.close();
		}

	}
}