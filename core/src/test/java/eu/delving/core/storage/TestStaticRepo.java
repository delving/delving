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
import eu.delving.core.storage.impl.StaticRepoImpl;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Make sure the page repo is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestStaticRepo {
    private static final String TEST_DB_NAME = "test-static";

    @Autowired
    private Mongo mongo;

    @Autowired
    private StaticRepoImpl repo;

    @Before
    public void before() throws UnknownHostException {
        mongo = new Mongo();
        repo = new StaticRepoImpl();
        repo.setMongo(mongo);
        repo.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void putGet() {
        String path = "/test/path/gumby.dml";
        // insert a doc
        repo.putPage(path, "Gumby Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(null));
        // insert a second for that path
        repo.putPage(path, "Gumby Really Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby Really Rulez", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 2 documents", 2, repo.getPageVersions(path).size());
        // insert a third for that path
        repo.putPage(path, "Gumby Really Truly Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby Really Truly Rulez", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 3 documents", 3, repo.getPageVersions(path).size());
        // remove the latest
        repo.putPage(path, null, null);
        repo.putPage(path, null, null);
        Assert.assertEquals("Should be 1 document", 1, repo.getPageVersions(path).size());
        // find the original
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(null));
    }

    @Test
    public void putGetLocale() {
        String path = "/test/path/gumby.dml";
        Locale locale = new Locale("no");
        // insert a doc
        repo.putPage(path, "Gumby No Locale", null);
        ObjectId id = repo.putPage(path, "Gumby Rulez", locale);
        repo.approve(path, id.toString());
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(locale));
        Assert.assertEquals("Unable to fetch", "Gumby No Locale", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 1 document", 1, repo.getPageVersions(path).size());
    }
}
