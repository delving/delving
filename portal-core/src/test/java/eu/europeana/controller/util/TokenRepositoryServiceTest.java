package eu.europeana.controller.util;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.rememberme.PersistentRememberMeToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author Vitali Kiruta
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml"})
public class TokenRepositoryServiceTest
{
    private static final Logger LOG = Logger.getLogger(TokenRepositoryServiceTest.class);

    @Autowired
    private TokenRepositoryService tokenRepositoryService;

    @Test
    public void testToken()
    {
        Date now = new Date();
        PersistentRememberMeToken t = new PersistentRememberMeToken (
                "user1", "series1", "token1", now
        );

        tokenRepositoryService.createNewToken(t);
        PersistentRememberMeToken t2 = tokenRepositoryService.getTokenForSeries("series1");
        Assert.assertEquals(t.getSeries(), t2.getSeries());
        Assert.assertEquals(t.getTokenValue(), t2.getTokenValue());
        Assert.assertEquals(t.getUsername(), t2.getUsername());
        tokenRepositoryService.removeUserTokens("user1");

    }
}
