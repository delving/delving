/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */
package eu.delving.core.storage;

import com.mongodb.Mongo;
import eu.delving.core.storage.impl.TokenRepoImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.net.UnknownHostException;
import java.util.Date;

/**
 * Make sure the page repo is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestTokenRepo {
    private static final String TEST_DB_NAME = "test-token";

    @Autowired
    private Mongo mongo;

    @Autowired
    private TokenRepoImpl repo;

    @Before
    public void before() throws UnknownHostException {
        mongo = new Mongo();
        repo = new TokenRepoImpl();
        repo.setMongo(mongo);
        repo.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void registration() {
        TokenRepo.RegistrationToken tok = repo.createRegistrationToken("gumby@delving.eu");
        Assert.assertEquals("email", "gumby@delving.eu", tok.getEmail());
        Assert.assertEquals("id size", 24, tok.getId().length());
        String id = tok.getId();
        tok = repo.getRegistrationToken(id);
        Assert.assertEquals("email", "gumby@delving.eu", tok.getEmail());
        tok.delete();
        tok = repo.getRegistrationToken(id);
        Assert.assertNull("should be gone", tok);
    }

    @Test
    public void authentication() {
        repo.createNewToken(new PersistentRememberMeToken("gumby@delving.eu", "world", createTokenValue(), new Date()));
        PersistentRememberMeToken tok = repo.getTokenForSeries("world");
        Assert.assertEquals("user name", "gumby@delving.eu", tok.getUsername());
        repo.updateToken("world", "TOKY", new Date(10000000L));
        tok = repo.getTokenForSeries("world");
        Assert.assertEquals("user name", "gumby@delving.eu", tok.getUsername());
        Assert.assertEquals("token value", "TOKY", tok.getTokenValue());
    }

    private String createTokenValue() {
        StringBuilder out = new StringBuilder();
        for (int walk=0; walk<15; walk++) {
            out.append((char) ('A' + (int)(Math.random() * 26)));
        }
        return out.toString();
    }
}
