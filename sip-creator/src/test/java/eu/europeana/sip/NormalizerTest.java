package eu.europeana.sip;

import eu.europeana.sip.xml.Normalizer;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 *  Basic test for normalizer.
 *
 * @author Borys Omelayenko
 */

public class NormalizerTest {

    private static final String TEST_FILE_NAME = "92001_Ag_EU_TELtreasures";

    @Test
    public void testBasic() throws Exception {


        File workDir = new File(getClass().getResource("/" + TEST_FILE_NAME).getFile());
        File actualOutputDir = new File(workDir, "output_xml");
        File expectedOutputDir = new File(workDir, "expected_output_xml");
        Normalizer normalizer = new Normalizer(
                workDir,
                actualOutputDir,
                false
        );

        normalizer.run();

        String expectedOutput = FileUtils.readFileToString(new File(expectedOutputDir, TEST_FILE_NAME + ".xml"));
        String actualOutput = FileUtils.readFileToString(new File(actualOutputDir, TEST_FILE_NAME + ".xml"));
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testNegativeKey() throws Exception {
        File workDir = new File(getClass().getResource("/" + TEST_FILE_NAME).getFile());
        try {
            Normalizer.loadProfile(new File(workDir, "profile/profile-negative-test.xml"));
            Assert.fail();
        } catch (Exception e) {
            // ok
        }
    }
}
