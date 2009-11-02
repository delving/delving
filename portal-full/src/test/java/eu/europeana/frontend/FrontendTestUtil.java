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
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.IOException;

/**
 * @author Vitali Kiruta
 */
public class FrontendTestUtil {

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

}
