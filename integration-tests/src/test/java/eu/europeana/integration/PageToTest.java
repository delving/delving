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

import org.junit.Ignore;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Integration tests running against an external system.
 * 
 * @author Borys Omelayenko
 */
@Ignore
public class PageToTest {
	HtmlPage page;
	String lang;
	String url;
	BrowserVersion browser;

	public void setPage(HtmlPage page) {
		this.page = page;
	}
	public HtmlPage getPage() {
		return page;
	}
	public String getLang() {
		return lang;
	}
	public String getUrl() {
		return url;
	}
	public BrowserVersion getBrowser() {
		return browser;
	}
	public PageToTest(HtmlPage page, String lang, String url, BrowserVersion browser) {
		this.lang = lang;
		this.url = url;
		this.browser = browser;
		this.page = page;
	}
	@Override
	public String toString() {
		return "PageToTest [url=" + url + ", lang=" + lang + ", browser=" + browser.getApplicationName() + "."  + browser.getApplicationVersion() + "]";
	}

}