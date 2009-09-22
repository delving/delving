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
import com.gargoylesoftware.htmlunit.html.*;
import eu.europeana.PortalFull;
import eu.europeana.controller.util.TokenService;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.Token;
import eu.europeana.database.domain.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author vitali
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/database-application-context.xml", "/core-application-context.xml"})
public class ForgotPasswordTest {

    private static final String NEW_PASSWORD = "new" + FrontendTestUtil.PASSWORD;
    private Server server;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserDao userDao;

    @Before
    public void start() throws Exception {
        server = new PortalFull().startServer();
    }

    @After
    public void stop() throws Exception {
        server.stop();
    }

    @Test
    public void changePassword() throws Exception {
        createUser();

        WebClient webClient = FrontendTestUtil.createWebClient();

        // 1.
        //go to myeuropeana.
        //use a separate server to request password reset.
        Server forgotServer = new PortalFull().startServer(8082);
        HtmlPage loginPage = webClient.getPage("http://localhost:8082/portal/myeuropeana.html");

        HtmlAnchor forgotPassword = (HtmlAnchor) loginPage.getElementById("forgotPassword");
        loginPage = forgotPassword.click();

        //fill in and submit forgot-password-request form
        HtmlTextInput registerEmail = (HtmlTextInput) loginPage.getElementById("forgot_email");
        registerEmail.setValueAttribute(FrontendTestUtil.EMAIL);

        HtmlSubmitInput register = (HtmlSubmitInput) loginPage.getElementById("submit_forgot");
        HtmlPage successPage = register.click();

        //assert success message
        System.out.println("++++ " + successPage.getWebResponse().getContentAsString());
        HtmlElement successMessage = successPage.getElementById("forgotSuccess");
        Assert.assertNotNull(successMessage);
        forgotServer.stop();

        // 2.
        //retrive newly generated change-password token
        Token token = tokenService.getTokenByEmail(FrontendTestUtil.EMAIL);

        //follow registration url (from the new browser window)
        //and on a different server.
        Server changePasswordServer = new PortalFull().startServer(8084);
        HtmlPage registerSuccessPage = changePassword(token, 8084);
        changePasswordServer.stop();

        //assert successful registration message
        Assert.assertNotNull( registerSuccessPage.getElementById("register_success"));

        // 3.
        //Now we can login. Do it from yet another browser window
        //and on a different server
        webClient = FrontendTestUtil.createWebClient();
        HtmlPage loginSuccessPage = FrontendTestUtil.login(webClient, FrontendTestUtil.EMAIL, NEW_PASSWORD);
        Assert.assertEquals("http://localhost:8080/portal/", loginSuccessPage.getWebResponse().getRequestUrl().toExternalForm());

        //remove user and token
        userDao.removeUser( userDao.fetchUserByEmail(FrontendTestUtil.EMAIL) );
        tokenService.removeToken(token);
    }

    private void createUser() {
        User user = new User();
        user.setEmail( FrontendTestUtil.EMAIL );
        user.setUserName( FrontendTestUtil.USERNAME );
        user.setPassword( FrontendTestUtil.PASSWORD );
        user.setEnabled( true );
        user.setRegistrationDate( new Date() );
        user.setRole( Role.ROLE_USER );
        userDao.addUser( user );
    }


    /**
     * @return change-password success page
     */
    private HtmlPage changePassword(Token token, int port) throws Exception {
        WebClient webClient = FrontendTestUtil.createWebClient();

        HtmlPage registrationPage = webClient.getPage(formatUrl("http://localhost:" + port + "/portal/change-password.html", token.getToken()));

        //fill in change-password form
        HtmlPasswordInput password = (HtmlPasswordInput) registrationPage.getElementById("password");
        password.setValueAttribute( NEW_PASSWORD );

        HtmlPasswordInput password2 = (HtmlPasswordInput) registrationPage.getElementById("password2");
        password2.setValueAttribute( NEW_PASSWORD );

        HtmlSubmitInput submit = (HtmlSubmitInput) registrationPage.getElementById("submit");
        return submit.click();
    }

    //TODO duplication with TokenReplyEmailSender
    private String formatUrl(String url, String token) {
        return url + "?token=" + token;
    }
}
