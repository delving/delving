package eu.europeana.controller.util;

import eu.europeana.database.domain.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vitali Kiruta
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml"})
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Test
    public void createToken() {
        tokenService.createNewToken("test@example.com");

        Token token = tokenService.getTokenByEmail("test@example.com");
        Assert.assertNotNull(token);

        tokenService.removeToken(token);
        long tokenCount = tokenService.countTokens();
        Assert.assertEquals(0, tokenCount);
    }
}
