package eu.delving.web.controller;

import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    private StaticRepo staticRepo;

    @Before
    public void before() {
        mongo.dropDatabase(TEST_DB_NAME);
        staticRepo.setDatabaseName(TEST_DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void putGet() {
        String path = "/test/path/gumby.dml";
        // insert a doc
        staticRepo.putPage(path, "Gumby Rulez");
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", staticRepo.getPage(path).getContent());
        // insert a second for that path
        staticRepo.putPage(path, "Gumby Really Rulez");
        Assert.assertEquals("Unable to fetch", "Gumby Really Rulez", staticRepo.getPage(path).getContent());
        Assert.assertEquals("Should be 2 documents", 2, staticRepo.getPageVersions(path).size());
        // insert a third for that path
        staticRepo.putPage(path, "Gumby Really Truly Rulez");
        Assert.assertEquals("Unable to fetch", "Gumby Really Truly Rulez", staticRepo.getPage(path).getContent());
        Assert.assertEquals("Should be 3 documents", 3, staticRepo.getPageVersions(path).size());
        // remove the latest
        staticRepo.putPage(path, null);
        staticRepo.putPage(path, null);
        Assert.assertEquals("Should be 1 document", 1, staticRepo.getPageVersions(path).size());
        // find the original
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", staticRepo.getPage(path).getContent());
    }
}
