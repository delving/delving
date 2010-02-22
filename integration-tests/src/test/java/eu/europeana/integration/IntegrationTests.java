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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.fileupload.util.Streams;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Integration tests running against an external system.
 * 
 * @author Borys Omelayenko
 */

public class IntegrationTests {

	public static String getTestSystemUrl()  {
		return "http://test1.europeana.sara.nl/portal/";
	}

	public static HtmlPage getPortalPage() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		return webClient.getPage(getTestSystemUrl());
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