package eu.delving.core.storage;

import com.mongodb.Mongo;
import eu.delving.core.storage.impl.StaticRepoImpl;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Locale;

/**
 * Make sure the page repo is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/core-application-context.xml",
        "/test-portal-application-context.xml"
})

public class TestStaticRepo {
    private static final String TEST_DB_NAME = "test-static";

    @Autowired
    private Mongo mongo;

    @Autowired
    private StaticRepoImpl repo;

    @Before
    public void before() {
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
