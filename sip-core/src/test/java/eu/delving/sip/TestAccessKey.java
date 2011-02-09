package eu.delving.sip;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Make sure the services key is working as planned
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestAccessKey {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void check() {
        String keyString = AccessKey.createKey("uzer", "delving-pass");
        log.info("Created "+keyString);
        AccessKey accessKey = new AccessKey();
        accessKey.setServicesPassword("delving-pass");
        Assert.assertTrue("Should have matched!", accessKey.checkKey(keyString));
        Assert.assertFalse("Should not have matched", accessKey.checkKey("gumby"));
    }
}
