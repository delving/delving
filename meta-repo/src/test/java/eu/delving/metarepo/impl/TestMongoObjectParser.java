package eu.delving.metarepo.impl;

import com.mongodb.DBObject;
import eu.delving.metarepo.core.MetaRepo;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Make sure the MongoObjectParser is working correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMongoObjectParser {
    private static final Logger LOG = Logger.getLogger(TestMongoObjectParser.class);
    private static final String XML = "/92017_Ag_EU_TEL_a0233E.xml";
    private static final QName RECORD_ROOT = QName.valueOf("{http://www.openarchives.org/OAI/2.0/}record");
    private static final QName UNIQUE_ELEMENT = QName.valueOf("{http://www.openarchives.org/OAI/2.0/}identifier");

    @Test
    public void simple() throws XMLStreamException, IOException {
        InputStream input = getClass().getResourceAsStream(XML);
        MongoObjectParser parser = new MongoObjectParser(input, RECORD_ROOT, UNIQUE_ELEMENT);
        DBObject object;
        while ((object = parser.nextRecord()) != null) {
            assertNotNull("Object", object);
            assertNotNull("Metadata", object.get(MetaRepo.Record.ORIGINAL));
            assertNull("Modified", object.get(MetaRepo.Record.MODIFIED));
            LOG.info(object.get(MetaRepo.Record.UNIQUE));
//            for (String line : object.get(Constant.ORIGINAL).toString().split("\n")) {
//                LOG.info(line);
//            }
        }
        parser.close();
        DBObject namespaces = parser.getNamespaces();
        Assert.assertEquals("Namespace count", 2, namespaces.keySet().size());
        LOG.info("namespaces:\n"+namespaces);
    }
}