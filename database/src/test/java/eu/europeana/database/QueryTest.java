package eu.europeana.database;

import eu.europeana.database.domain.User;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/database-application-context.xml", "/test-application-context.xml"})
public class QueryTest {
    private static final Logger LOG = Logger.getLogger(QueryTest.class);

    @Autowired
    private UserDao userDao;

    @Test
    public void getIds() throws Exception {
        long time = System.currentTimeMillis();
        // todo: fix this test method
//        List<EuropeanaId> idList = eseDao.getEuropeanaIdsForIndexing(1000);
//        LOG.info(idList.size()+" imported ids found in time "+(System.currentTimeMillis()-time));
    }

    @Test
    public void getTagCounts() throws Exception {
        List<TagCount> counts = userDao.getSocialTagCounts("n");
        LOG.info("first:");
        for (TagCount count : counts) {
            LOG.info(count);
        }
        counts = userDao.getSocialTagCounts("boo");
        LOG.info("second:");
        for (TagCount count : counts) {
            LOG.info(count);
        }
    }

    @Test
    public void fetchUsers() throws Exception {
        List<User> users = userDao.fetchUsers("g");
        LOG.info("users:");
        for (User user : users) {
            LOG.info(user.getEmail());
        }
    }

}