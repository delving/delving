package eu.europeana.json;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

import java.text.MessageFormat;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 15, 2009: 10:53:27 PM
 */
public class JsonUtilTest {
    private String stringWithoutUrl = "bla bla";
    private String emptyString = " ";
    private String stringWithUrl = "http://www.europeana.eu/portal/index.html";

    @Test
    public void testInsertHrefAroundURL() {
        assertEquals("String should not have been modified",
                stringWithoutUrl,
                JsonUtil.insertAHrefs(stringWithoutUrl));
        assertEquals("Empty string should be unmodified",
                emptyString,
                JsonUtil.insertAHrefs(emptyString));
        assertEquals("Href was not inserted into string",
                MessageFormat.format("<a href=\"{0}\">{0}</a>", stringWithUrl),
                JsonUtil.insertAHrefs(stringWithUrl));
        String testString = "see " + stringWithUrl + ".";
        assertEquals("Href was not inserted into string",
                MessageFormat.format("see <a href=\"{0}\">{0}.</a>", stringWithUrl),
                JsonUtil.insertAHrefs(testString));
        testString = "See " + stringWithUrl + " and see " + stringWithUrl;
        assertEquals("Href was not inserted into string",
                MessageFormat.format("See <a href=\"{0}\">{0}</a> and see <a href=\"{0}\">{0}</a>", stringWithUrl),
                JsonUtil.insertAHrefs(testString));
    }
}
