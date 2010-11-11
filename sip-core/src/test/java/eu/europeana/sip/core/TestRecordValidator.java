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

    private String toString(String [] array) {
        StringBuilder out = new StringBuilder();
        for (String line : array) {
            out.append(line.trim()).append('\n');
        }
        return out.toString();
    }

    private void test(String message, String[] expectArray, String[] inputArray) {
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

    @Test
    public void duplicateRemoval() {
        test(
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

    /*
    private void compareList(List<String> given, List<String> expect, String problemString) {
        StringBuilder input = new StringBuilder();
        for (String line : given) {
            input.append(line).append('\n');
        }
//        try {
            List<FieldEntry> fieldEntries = FieldEntry.createList(input.toString());
//            recordValidator.validate(null, fieldEntries);
            String result = FieldEntry.toString(fieldEntries, false);
            StringBuilder expected = new StringBuilder();
            for (String line : expect) {
                expected.append(line).append('\n');
            }
            assertEquals(expected.toString(), result);
            assertEquals("Didn't experience expected problem: " + problemString, null, problemString);
            if (problemString != null) {

            }
//        }
//        catch (RecordValidationException e) {
//            log.info(e);
//            assertEquals("Multiple problems, but these unit tests expect to cause only one!\n"+e, 1, e.getProblems().size());
//            assertEquals("Didn't see expected problem string: ["+problemString+"]", true, e.getProblems().get(0).contains(problemString));
//        }
    }

    private void compare(String[] given, String[] expect, String problemString) {
        List<String> givenList = new ArrayList<String>(Arrays.asList(given));
        List<String> expectList = new ArrayList<String>(Arrays.asList(expect));
        givenList.addAll(validFields);
        Collections.sort(givenList);
        expectList.addAll(validFields);
        Collections.sort(expectList);
        compareList(givenList, expectList, problemString);
    }

    private void compare(String problemString){
        compare(new String[]{},new String[]{}, problemString);
    }

    @Test
    public void missingIsShownXx() throws RecordValidationException {
        validFields.remove(0);
        compare("[europeana:isShownAt or europeana:isShownBy]");
    }

    @Test
    public void duplicateOrEmpty() throws RecordValidationException {
        compare(
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:creator>God</dc:creator>",
                        "<dc:creator></dc:creator>",
                        "<dc:identifier>one</dc:identifier>",
                }
                ,
                new String[]{
                        "<dc:creator>God</dc:creator>",
                        "<dc:identifier>one</dc:identifier>"
                }
                ,
                null
        );
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
