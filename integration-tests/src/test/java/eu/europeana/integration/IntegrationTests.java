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

public class IntegrationTests implements Iterable<PageToTest> {


	private static BrowserVersion[] browsers = {

		BrowserVersion.FIREFOX_3,
		BrowserVersion.INTERNET_EXPLORER_7,
		BrowserVersion.INTERNET_EXPLORER_8
	};

	List<PageToTest> list = new ArrayList<PageToTest>();

    private static IntegrationTests tests = null;
	public static IntegrationTests portals() throws Exception {
		if (tests == null) {
			tests = new IntegrationTests();
		}
		return tests;
	}
	
	private IntegrationTests() throws Exception {
		// list of pages
		String url = null;

		try {
			for (BrowserVersion browser : browsers) {
				// get page
				WebClient webClient = new WebClient(browser);
				webClient.setAjaxController(new NicelyResynchronizingAjaxController());
				webClient.setJavaScriptEnabled(true);
				
				url = FrontendTestUtil.testPortalUrl();
				HtmlPage page = webClient.getPage(url);

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
					list.add(new PageToTest(page, lang, url, browser));	
				}
				
			}
		} catch (IOException e) {
			throw new IOException("On url " + url, e);
		}

		if (list.size() > 30 * browsers.length) {
			throw new IOException("Too many languages: " + list.size());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<PageToTest> iterator() {
		return new AbstractIteratorDecorator(list.iterator()) {

			@Override
			public Object next() {
				PageToTest page = (PageToTest) super.next();
				HtmlSelect inputByName = page.getPage().getElementByName("dd_lang");
				HtmlPage langPage = page.getPage();
				if (page.getLang() != null) {
					langPage = (HtmlPage)inputByName.setSelectedAttribute(page.getLang(), true);
				}
				return new PageToTest(langPage, page.getLang(), page.getUrl(), page.getBrowser());
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