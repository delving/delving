package eu.delving.core.storage;

import com.mongodb.Mongo;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/core-application-context.xml"
})

public class TestPersonStorage {
    private static final String TEST_DB_NAME = "test-person";

    @Autowired
    private Mongo mongo;

    @Autowired
    private PersonStorageImpl personStorage;

    @Before
    public void before() {
        personStorage.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
        PersonStorage.Person person = personStorage.createPerson("dude@delving.eu");
        person.setEnabled(true);
        person.setFirstName("Joe");
        person.setPassword("gumby");
        person.setLastName("Dude");
        person.setLastLogin(new Date());
        person.save();
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void authenticate() {
        PersonStorage.Person dude = personStorage.authenticate("dude@delving.eu", "gumbi");
        Assert.assertNull(dude);
        dude = personStorage.authenticate("dude@delving.eu", "gumby");
        Assert.assertNotNull(dude);
    }
}
