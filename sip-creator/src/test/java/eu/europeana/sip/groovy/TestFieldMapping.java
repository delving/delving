package eu.europeana.sip.groovy;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Make sure the field mappings work
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestFieldMapping {

    @Test
    public void parseTest() {
        FieldMapping fm = new FieldMapping("VERBATIM{var_one,varTwo}{field0}");
        Assert.assertEquals("VERBATIM{var_one,varTwo}{field0}", fm.toString());
    }

}
