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
import eu.delving.core.storage.impl.UserRepoImpl;
import eu.delving.domain.Language;
import eu.europeana.core.querymodel.query.DocType;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestUserRepo {
    private static final String TEST_DB_NAME = "test-person";
    private static final String EMAIL = "dude@delving.eu";

    private Mongo mongo;
    private UserRepoImpl userRepo;

    @Before
    public void before() throws UnknownHostException {
        mongo = new Mongo();
        userRepo = new UserRepoImpl();
        userRepo.setMongo(mongo);
        userRepo.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
        User user = userRepo.createUser(EMAIL);
        user.setEnabled(true);
        user.setUserName("gumby");
        user.setFirstName("Joe");
        user.setPassword("gumby");
        user.setLastName("Dude");
        user.setLastLogin(new Date());
        user.save();
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void authenticate() {
        User dude = userRepo.authenticate(EMAIL, "gumbi");
        Assert.assertNull(dude);
        dude = userRepo.authenticate(EMAIL, "gumby");
        Assert.assertNotNull(dude);
    }

    @Test
    public void changeField() {
        User dude = userRepo.byEmail(EMAIL);
        dude.setFirstName("Mary");
        dude.save();
        dude = userRepo.byEmail(EMAIL);
        Assert.assertEquals("New name didn't hold", "Mary", dude.getFirstName());
    }

    @Test
    public void addRemoveItem() {
        User dude = userRepo.byEmail(EMAIL);
        dude.addItem("Author", "Title", Language.NO, "delvingId", "europeanaId", DocType.IMAGE, "thumbnail");
        dude.save();
        dude = userRepo.byEmail(EMAIL);
        List<User.Item> items = dude.getItems();
        Assert.assertEquals("Should be one item", 1, items.size());
        User.Item item = items.get(0);
        Assert.assertEquals("field wrong", "Author", item.getAuthor());
        Assert.assertEquals("field wrong", "Title", item.getTitle());
        Assert.assertEquals("field wrong", Language.NO, item.getLanguage());
        item.remove();
        dude.save();
        dude = userRepo.byEmail(EMAIL);
        Assert.assertEquals("Should be empty", 0, dude.getItems().size());
    }

    @Test
    public void addRemoveSearch() {
        User dude = userRepo.byEmail(EMAIL);
        dude.addSearch("Query", "QueryString", Language.FI);
        dude.save();
        dude = userRepo.byEmail(EMAIL);
        List<User.Search> searches = dude.getSearches();
        Assert.assertEquals("Should be one item", 1, searches.size());
        User.Search search = searches.get(0);
        Assert.assertEquals("field wrong", "Query", search.getQuery());
        Assert.assertEquals("field wrong", "QueryString", search.getQueryString());
        Assert.assertEquals("field wrong", Language.FI, search.getLanguage());
        search.remove();
        dude.save();
        dude = userRepo.byEmail(EMAIL);
        Assert.assertEquals("Should be empty", 0, dude.getSearches().size());
    }

    @Test
    public void userName() {
        Assert.assertTrue("Should exist", userRepo.isExistingUserName("gumby"));
        Assert.assertFalse("Shouldn't exist", userRepo.isExistingUserName("pokey"));
        Assert.assertTrue("improper", userRepo.isProperUserName("1dude"));
        Assert.assertTrue("improper", userRepo.isProperUserName("_babe"));
        Assert.assertTrue("improper", userRepo.isProperUserName("monkey_5"));
        Assert.assertFalse("improper", userRepo.isProperUserName("who me"));
        Assert.assertFalse("improper", userRepo.isProperUserName("who__me"));
        Assert.assertFalse("improper", userRepo.isProperUserName("Caps"));
    }
}
