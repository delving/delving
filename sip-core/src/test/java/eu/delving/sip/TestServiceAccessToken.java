package eu.delving.sip;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Make sure the services key is working as planned
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestServiceAccessToken {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void check() {
        String keyString = ServiceAccessToken.createKey("uzer", "delving-pass");
        log.info("Created "+keyString);
        ServiceAccessToken serviceAccessToken = new ServiceAccessToken();
        serviceAccessToken.setServicesPassword("delving-pass");
        Assert.assertTrue("Should have matched!", serviceAccessToken.checkKey(keyString));
        Assert.assertFalse("Should not have matched", serviceAccessToken.checkKey("gumby"));
    }
}
