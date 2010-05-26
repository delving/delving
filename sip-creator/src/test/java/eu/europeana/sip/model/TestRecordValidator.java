package eu.europeana.sip.model;

import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.definitions.beans.AllFieldBean;
import eu.europeana.sip.xml.RecordValidationException;
import eu.europeana.sip.xml.RecordValidator;
import org.junit.Before;
import org.junit.Test;

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
 * <li> isShownAt and isShownBy required
 * <li> isShownBy, isShownAt, and europeana_object should contain an url
 * <li> records with duplicate europeana_uri's should be discarded.
 * <li> europeana_uri: must be unique per collection and can only occur once
 * <li> europeana_type: can only have four values that must be checked from enum and can only occur once
 * <li> europeana_year: contains a 4 digit year. can appear multiple times
 * <li> europeana_language: must come from enum. multivalue true
 * <li> europeana_country: can only occur once and must be the same for every record
 * <li> europeana_provider: must be a constant. can only occur one per record.
 * <li> europeana_collectionName: must be a constant per collection. Can only occur once per record
 * </ul>
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestRecordValidator {
    private static final String VALID_TYPE = "<europeana:type>type</europeana:type>";
    private static final String VALID_OBJECT = "<europeana:object>object</europeana:object>";
    private static final String VALID_SHOWN_AT = "<europeana:isShownAt>is-shown-at</europeana:isShownAt>";
    private static final String VALID_SHOWN_BY = "<europeana:isShownBy>is-shown-by</europeana:isShownBy>";
    private RecordValidator recordValidator;

    @Before
    public void prepare() {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(AllFieldBean.class);
        AnnotationProcessorImpl ap = new AnnotationProcessorImpl();
        ap.setClasses(classes);
        recordValidator = new RecordValidator(ap, true);
    }

    private void compareList(List<String> given, List<String> expect) throws RecordValidationException {
        StringBuilder input = new StringBuilder();
        for (String line : given) {
            input.append(line).append('\n');
        }
        String result = recordValidator.validate(input.toString());
        StringBuilder expected = new StringBuilder();
        expected.append("<record>\n");
        for (String line : expect) {
            expected.append("   ").append(line).append('\n');
        }
        expected.append("</record>\n");
        assertEquals(expected.toString(), result);
    }

    private void compare(String [] given, String [] expect) throws RecordValidationException {
        List<String> givenList = new ArrayList<String>(Arrays.asList(given));
        List<String> expectList = new ArrayList<String>(Arrays.asList(expect));
        givenList.add(VALID_SHOWN_AT);
        expectList.add(VALID_SHOWN_AT);
        givenList.add(VALID_SHOWN_BY);
        expectList.add(VALID_SHOWN_BY);
        givenList.add(VALID_OBJECT);
        expectList.add(VALID_OBJECT);
        givenList.add(VALID_TYPE);
        expectList.add(VALID_TYPE);
        compareList(givenList, expectList);
    }

    private void compareBare(String [] given, String [] expect) throws RecordValidationException {
        List<String> givenList = Arrays.asList(given);
        List<String> expectList = Arrays.asList(expect);
        compareList(givenList, expectList);
    }

    @Test
    public void duplicateOrEmpty() throws RecordValidationException {
        compare(
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:creator>God</dc:creator>",
                        "<dc:creator></dc:creator>",
                        "<dc:identifier>one</dc:identifier>"
                }
                ,
                new String[]{
                        "<dc:creator>God</dc:creator>",
                        "<dc:identifier>one</dc:identifier>"
                }
        );
    }

    @Test(expected = RecordValidationException.class)
    public void spuriousTag() throws RecordValidationException {
        compare(
                new String[]{
                        "<description>illegal</description>"
                }
                ,
                new String [] {}
        );
    }

//    @Test(expected = RecordValidationException.class)
    public void shownTags() throws RecordValidationException {

    }
}
