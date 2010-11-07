package eu.delving.core.util;

import com.mongodb.Mongo;
import eu.delving.core.util.MessageSourceRepo;
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

public class TestMessageSourceRepo {
    private static final String TEST_DB_NAME = "test-message";

    @Autowired
    private Mongo mongo;

    @Autowired
    private MessageSourceRepo repo;

    @Before
    public void before() {
        mongo.dropDatabase(TEST_DB_NAME);
        repo.setDatabaseName(TEST_DB_NAME);
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
