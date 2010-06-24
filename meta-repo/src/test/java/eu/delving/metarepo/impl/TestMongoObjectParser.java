package eu.delving.metarepo.impl;

import com.mongodb.DBObject;
import com.thoughtworks.xstream.XStream;
import eu.delving.metarepo.core.MetaRepo;
import eu.europeana.sip.core.DataSetDetails;
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
    private static final String DETAILS = "/sffDf.xml.details";
    private static final String XML = "/sffDf.xml";

    @Test
    public void simple() throws XMLStreamException, IOException {
        XStream xs = new XStream();
        xs.processAnnotations(DataSetDetails.class);
        DataSetDetails details = (DataSetDetails)xs.fromXML(getClass().getResourceAsStream(DETAILS));
        QName recordRoot = QName.valueOf(details.getRecordRoot());
        QName uniqueElement = QName.valueOf(details.getUniqueElement());
        InputStream input = getClass().getResourceAsStream(XML);
        MongoObjectParser parser = new MongoObjectParser(input, recordRoot, uniqueElement);
        DBObject object;
        int count = 30;
        while ((object = parser.nextRecord()) != null) {
            assertNotNull("Object", object);
            assertNotNull("Metadata", object.get(MetaRepo.Record.ORIGINAL));
            assertNull("Modified", object.get(MetaRepo.Record.MODIFIED));
            LOG.info(object.get(MetaRepo.Record.UNIQUE));
            if (count-- == 0) {
                break;
            }
        }
        parser.close();
        DBObject namespaces = parser.getNamespaces();
        LOG.info("namespaces:\n"+namespaces);
        Assert.assertEquals("Namespace count", 4, namespaces.keySet().size());
    }
}