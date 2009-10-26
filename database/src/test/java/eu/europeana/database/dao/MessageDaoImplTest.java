package eu.europeana.database.dao;

import eu.europeana.database.MessageDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 18, 2009: 3:29:31 PM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/database-application-context.xml", "/hypersonic-datasource.xml"})
public class MessageDaoImplTest {

    @Autowired
    private MessageDao messageDao;


    @PersistenceContext
    protected EntityManager entityManager;

    @Test
    public void testFetchStaticPage() {
        StaticPage newStaticPage = messageDao.fetchStaticPage(Language.EN, "aboutus");
        Assert.assertNotNull(newStaticPage);
    }
}
