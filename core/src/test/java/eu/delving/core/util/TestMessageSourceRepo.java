/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.delving.core.util;

import com.mongodb.Mongo;
import org.junit.*;

import java.util.Locale;

/**
 * Make sure the page repo is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Ignore
public class TestMessageSourceRepo {
    private static final String TEST_DB_NAME = "test-message";

    private Mongo mongo;

    private MessageSourceRepo repo;

    @Before
    public void before() throws Exception {
        repo = new MessageSourceRepo();
        final MongoFactory mongoFactory = new MongoFactory();
        mongoFactory.setTestContext("true");
        mongoFactory.afterPropertiesSet();
        repo.setMongoFactory(mongoFactory);
        mongo = mongoFactory.getMongo();
        repo.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void fetchAdjustFetch() {
        String key = "Search_t";
        Assert.assertTrue(repo.getMessageFileMaps().isEmpty());
        // fetch the original
        String base = repo.resolveCodeWithoutArguments(key, null);
        Assert.assertEquals("Original unexpected value", "Search", base);
        Assert.assertTrue(repo.getMessageFileMaps().isEmpty());
        // change and fetch again
        repo.setTranslation(key, "Looky", null);
        base = repo.resolveCodeWithoutArguments(key, null);
        Assert.assertEquals("Changed unexpected value", "Looky", base);
        Assert.assertEquals("Should be one entry", 1, repo.getMessageFileMaps().size());
        // now another locale
        Locale locale = Locale.FRENCH;
        repo.setTranslation(key, "Alors!", locale);
        Assert.assertEquals("Should have remained the same", "Looky", repo.resolveCodeWithoutArguments(key, null));
        Assert.assertEquals("Should have been changed", "Alors!", repo.resolveCodeWithoutArguments(key, locale));
        Assert.assertEquals("Should be two enties", 2, repo.getMessageFileMaps().size());
        repo.getTranslation(key).remove();
        Assert.assertEquals("Should be zero enties", 0, repo.getMessageFileMaps().size());
        Assert.assertEquals("Original should have returned", "Search", repo.resolveCodeWithoutArguments(key, null));
    }
}
