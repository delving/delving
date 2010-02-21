package eu.europeana.core.database.incoming;

import org.junit.Assert;
import org.junit.Test;

/**
 * make sure the histogram makes sense
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestHistogram {

    @Test
    public void histogram() {
        Histogram histogram = new Histogram("Gumby");
        for (long walk = 0; walk < 109461; walk++) {
            histogram.recordDuration(walk);
        }
        Assert.assertEquals(
                "Histogram (Gumby) {\n" +
                    "\t000010: 0000010\n" +
                    "\t000020: 0000010\n" +
                    "\t000030: 0000010\n" +
                    "\t000050: 0000020\n" +
                    "\t000080: 0000030\n" +
                    "\t000130: 0000050\n" +
                    "\t000210: 0000080\n" +
                    "\t000340: 0000130\n" +
                    "\t000550: 0000210\n" +
                    "\t000890: 0000340\n" +
                    "\t001440: 0000550\n" +
                    "\t002330: 0000890\n" +
                    "\t003770: 0001440\n" +
                    "\t006100: 0002330\n" +
                    "\t009870: 0003770\n" +
                    "\t015970: 0006100\n" +
                    "\t025840: 0009870\n" +
                    "\t041810: 0015970\n" +
                    "\t067650: 0025840\n" +
                    "\t109460: 0041811\n" +
                "}\n",
                histogram.toString()
        );
    }

}
