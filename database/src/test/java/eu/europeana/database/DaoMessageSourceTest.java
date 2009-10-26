package eu.europeana.database;

import eu.europeana.database.domain.Language;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml",
        "/hypersonic-datasource.xml"
})
public class DaoMessageSourceTest {

    @Autowired
    private MessageDao messageDao;

    private DaoMessageSource daoMessageSource;

    @Before
    public void before() throws IOException {
        DataMigration migration = new DataMigration();
        migration.readTableFromResource(DataMigration.Table.TRANSLATION_KEYS);
        daoMessageSource = new DaoMessageSource();
        daoMessageSource.setMessageDao(messageDao);
    }

    @Test
    public void zoek() throws Exception {
        //todo: finish this test
        Locale nl = new Locale(Language.NL.getCode());
        String zoek = daoMessageSource.getMessage("Search_t", null, nl);
        Assert.assertEquals("Zoek", zoek);
    }
}