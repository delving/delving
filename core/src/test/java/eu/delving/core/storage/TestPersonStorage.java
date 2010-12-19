package eu.delving.core.storage;

import com.mongodb.Mongo;
import eu.delving.domain.Language;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

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
    public static final String EMAIL = "dude@delving.eu";

    @Before
    public void before() {
        personStorage.setDatabaseName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
        PersonStorage.Person person = personStorage.createPerson(EMAIL);
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
        PersonStorage.Person dude = personStorage.authenticate(EMAIL, "gumbi");
        Assert.assertNull(dude);
        dude = personStorage.authenticate(EMAIL, "gumby");
        Assert.assertNotNull(dude);
    }

    @Test
    public void changeField() {
        PersonStorage.Person dude = personStorage.byEmail(EMAIL);
        dude.setFirstName("Mary");
        dude.save();
        dude = personStorage.byEmail(EMAIL);
        Assert.assertEquals("New name didn't hold", "Mary", dude.getFirstName());
    }

    @Test
    public void addRemoveItem() {
        PersonStorage.Person dude = personStorage.byEmail(EMAIL);
        dude.addItem("Author", "Title", Language.NO);
        dude.save();
        dude = personStorage.byEmail(EMAIL);
        List<PersonStorage.Item> items = dude.getItems();
        Assert.assertEquals("Should be one item", 1, items.size());
        PersonStorage.Item item = items.get(0);
        Assert.assertEquals("field wrong", "Author", item.getAuthor());
        Assert.assertEquals("field wrong", "Title", item.getTitle());
        Assert.assertEquals("field wrong", Language.NO, item.getLanguage());
        item.remove();
        dude.save();
        dude = personStorage.byEmail(EMAIL);
        Assert.assertEquals("Should be empty", 0, dude.getItems().size());
    }
}
