package eu.europeana.sip.core;

import eu.delving.core.metadata.MetadataModelImpl;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test that the validator is working properly
 * <br>
 * <ul>
 * <li> no duplicate fields - filtered silently
 * <li> no empty fields - filtered silently
 * <li> no unknown fields
 * <li> required fields must be there (checked by groups, so "or" requirements are possible)
 * <li> non-multivalued fields must not have multiple values
 * <li> URLs checked using java.net.URL
 * <li> regular expression checking
 * <li> ids unique per collection
 * </ul>
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestRecordValidator {
    private static final String[] VALID_FIELDZ = {
            "<europeana:isShownAt>http://is-shown-at.com/</europeana:isShownAt>",
            "<europeana:uri>http://uri.com/</europeana:uri>",
            "<europeana:provider>provider</europeana:provider>",
            "<europeana:country>netherlands</europeana:country>",
            "<europeana:collectionName>collectionName</europeana:collectionName>",
            "<europeana:language>en</europeana:language>",
            "<europeana:object>http://object.com/</europeana:object>",
            "<europeana:rights>nerd's</europeana:rights>",
            "<europeana:dataProvider>everyone</europeana:dataProvider>",
            "<europeana:type>IMAGE</europeana:type>",
    };
    private Logger log = Logger.getLogger(getClass());
    private RecordValidator recordValidator;
    private List<String> problems = new ArrayList<String>();
    private List<String> validFields = new ArrayList<String>(Arrays.asList(VALID_FIELDZ));

    @Before
    public void prepare() throws IOException {
        MetadataModelImpl metadataModel = new MetadataModelImpl();
        metadataModel.setRecordDefinitionResource("/abm-record-definition.xml");
        recordValidator = new RecordValidator(metadataModel, true);
        problems.clear();
    }

    private String toString(String[] array) {
        StringBuilder out = new StringBuilder();
        for (String line : array) {
            out.append(line.trim()).append('\n');
        }
        return out.toString();
    }

    private void validate(String message, String[] expectArray, String[] inputArray) {
        String expect = toString(expectArray);
        String input = toString(inputArray);
        log.info("input:\n" + input);
        String validated = recordValidator.validate(input, problems);
        for (String problem : problems) {
            log.info("Problem: " + problem);
        }
        Assert.assertTrue("Problems", problems.isEmpty());
        validated = toString(validated.split("\n"));
        log.info("validated:\n" + validated);
        assertEquals(
                message,
                expect,
                validated
        );
    }

    private void problem(String [] inputArray, String problemContains) {
        String input = toString(inputArray);
        log.info("input:\n" + input);
        recordValidator.validate(input, problems);
        Assert.assertFalse("Expected problems", problems.isEmpty());
        boolean found = false;
        for (String problem : problems) {
            log.info("Problem: "+problem);
            if (problem.contains(problemContains)) {
                found = true;
            }
        }
        if (!found) {
            Assert.fail(String.format("Expected to find a problem containing [%s]", problemContains));
        }
    }

    @Test
    public void duplicateRemoval() {
        validate(
                "Duplicate not removed",
                new String[]{
                        "<record>",
                        "<dc:identifier>one</dc:identifier>",
                        "</record>"
                },
                new String[]{
                        "<record>",
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:identifier>one</dc:identifier>",
                        "</record>"
                }
        );
        Assert.assertTrue("Problems", problems.isEmpty());
    }

    @Test
    public void emptyRemoval() {
        validate(
                "Empty not removed",
                new String[]{
                        "<record>",
                        "<dc:identifier>one</dc:identifier>",
                        "</record>"
                },
                new String[]{
                        "<record>",
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:title></dc:title>",
                        "</record>"
                }
        );
        Assert.assertTrue("Problems", problems.isEmpty());
    }

    @Test
    public void optionsTrue() {
        validate(
                "Empty not removed",
                new String[]{
                        "<record>",
                        "<europeana:type>SOUND</europeana:type>",
                        "</record>"
                },
                new String[]{
                        "<record>",
                        "<europeana:type>SOUND</europeana:type>",
                        "</record>"
                }
        );
    }

    @Test
    public void optionsFalse() {
        problem(
                new String[]{
                        "<record>",
                        "<europeana:type>SOwUND</europeana:type>",
                        "</record>"
                },
                "which does not belong to"
        );
    }

    @Test
    public void noRecord() throws RecordValidationException {
        problem(
                new String[]{
                        "<europeana:title>illegal</europeana:title>"
                },
                "Missing record element"
        );
    }

    @Test
    public void spuriousTag() throws RecordValidationException {
        problem(
                new String[]{
                        "<record>",
                        "<description>illegal</description>",
                        "</record>"
                },
                "No field definition found"
        );
    }

    /*
    @Test
    public void missingIsShownXx() throws RecordValidationException {
        validFields.remove(0);
        compare("[europeana:isShownAt or europeana:isShownBy]");
    }

    @Test
    public void spuriousTag() throws RecordValidationException {
        compare(
                new String[]{
                        "<description>illegal</description>"
                }
                ,
                new String[]{
                },
                "Unknown XML"
        );
    }

    @Test
    public void tooManyForMultivaluedFalse() throws RecordValidationException {
        compare(
                new String[]{
                        "<europeana:type>SOUND</europeana:type>",
                }
                ,
                new String[]{
                        "<europeana:type>SOUND</europeana:type>",
                },
                "Single-valued field"
        );
    }

    @Test
    public void doubleUriOk() throws RecordValidationException {
        compare(null);
        validFields.set(1, "<europeana:uri>http://uri.com/asecondone</europeana:uri>");
        compare(null);
    }

    @Test
    public void doubleUriProblem() throws RecordValidationException {
        compare(null);
        compare("appears more than once");
    }

    @Test
    public void constantChanges() throws RecordValidationException {
        compare(null);
        validFields.set(1, "<europeana:uri>http://uri.com/asecondone</europeana:uri>");
        validFields.set(2,"<europeana:provider>another provider</europeana:provider>");
        compare("multiple values [another provider]");
    }
    */
}
