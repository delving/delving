package eu.europeana.sip.model;

import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.definitions.beans.AllFieldBean;
import eu.europeana.sip.xml.RecordValidationException;
import eu.europeana.sip.xml.RecordValidator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test that the validator is working properly
 * <br>
 * <ul>
 * <li> no duplicate fields - filtered silently
 * <li> no empty fields - filtered silently
 * <li> no unknown fields
 * <li> required fields must be there
 * <li> non-multivalued fields must not have multiple values
 * <li> URLs checked using java.net.URL
 * <li> regular expression checking
 * <li> ids unique per collection
 * <li> todo: europeana_type: can only have four values that must be checked from enum and can only occur once
 * <li> todo: europeana_language: must come from enum. multivalue true
 * <li> todo: europeana_country: can only occur once and must be the same for every record
 * <li> todo: europeana_provider: must be a constant. can only occur one per record.
 * <li> todo: europeana_collectionName: must be a constant per collection. Can only occur once per record
 * </ul>
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestRecordValidator {
    private static final String[] VALID_FIELDS = {
            "<europeana:country>country</europeana:country>",
            "<europeana:europeanaCollectionName>collectionName</europeana:europeanaCollectionName>",
            "<europeana:isShownAt>http://is-shown-at.com/</europeana:isShownAt>",
            "<europeana:isShownBy>http://is-shown-by.com/</europeana:isShownBy>",
            "<europeana:language>language</europeana:language>",
            "<europeana:object>http://object.com/</europeana:object>",
            "<europeana:provider>provider</europeana:provider>",
            "<europeana:type>type</europeana:type>",
            "<europeana:uri>http://uri.com/</europeana:uri>",
    };
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

    private void compare(String[] given, String[] expect) throws RecordValidationException {
        List<String> givenList = new ArrayList<String>(Arrays.asList(given));
        List<String> expectList = new ArrayList<String>(Arrays.asList(expect));
        givenList.addAll(Arrays.asList(VALID_FIELDS));
        Collections.sort(givenList);
        expectList.addAll(Arrays.asList(VALID_FIELDS));
        Collections.sort(expectList);
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
                new String[]{}
        );
    }

    @Test(expected = RecordValidationException.class)
    public void badYear() throws RecordValidationException {
        compare(
                new String[]{
                        "<europeana:year>99999</europeana:year>"
                }
                ,
                new String[]{
                        "<europeana:year>99999</europeana:year>"
                }
        );
    }

    @Test(expected = RecordValidationException.class)
    public void doubleUri() throws RecordValidationException {
        compare(new String[]{},new String[]{});
        compare(new String[]{},new String[]{});
    }
}
