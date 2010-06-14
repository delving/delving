package eu.delving.metarepo;

import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Make sure the DBObjectParser is working correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestParser {
    private static final Logger LOG = Logger.getLogger(TestParser.class);
    private static final String META_FORMAT = "ESE_NOT";
    private static final String XML = "/92017_Ag_EU_TEL_a0233E.xml";
    private static final String QNAME = "{http://www.openarchives.org/OAI/2.0/}record";

    @Test
    public void simple() throws XMLStreamException, IOException {
        InputStream input = getClass().getResourceAsStream(XML);
        QName qName = QName.valueOf(QNAME);
        DBObjectParser parser = new DBObjectParser(input, qName, META_FORMAT);
        for (int walk=0; walk<2; walk++) {
            DBObject object = parser.nextRecord();
            assertNotNull("Object", object);
            assertNotNull("Metadata", object.get(META_FORMAT));
            for (String line : object.get(META_FORMAT).toString().split("\n")) {
                LOG.info(line);
            }
        }
        parser.close();
    }
}