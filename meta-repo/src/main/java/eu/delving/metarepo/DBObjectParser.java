package eu.delving.metarepo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DBObjectParser {
    private InputStream inputStream;
    private XMLStreamReader2 input;
    private QName recordRoot;
    private String metadataFormat;

    public DBObjectParser(InputStream inputStream, QName recordRoot, String metadataFormat) throws XMLStreamException {
        this.inputStream = inputStream;
        this.recordRoot = recordRoot;
        this.metadataFormat = metadataFormat;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader(getClass().getName(), inputStream);
    }

    @SuppressWarnings("unchecked")
    public synchronized DBObject nextRecord() throws XMLStreamException, IOException {
        DBObject object = null;
        StringBuilder recordContent = new StringBuilder();
        boolean withinRecord = false;
        int depth = 0;
        int recordDepth = 0;
        boolean contentPresent = false;
        while (object == null) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    break;
                case XMLEvent.NAMESPACE:
                    break;
                case XMLEvent.START_ELEMENT:
                    depth++;
                    if (!withinRecord) {
                        if (input.getName().equals(recordRoot)) {
                            withinRecord = true;
                            recordDepth = depth;
                        }
                    }
                    else {
                        if (contentPresent) {
                            throw new IOException("Content and subtags not permitted");
                        }
                        recordContent.append("<").append(input.getPrefixedName());
                        if (input.getAttributeCount() > 0) {
                            for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                QName qName = input.getAttributeName(walk);
                                String attrName = qName.getPrefix().isEmpty() ? qName.getLocalPart() : qName.getPrefix() + ":" + qName.getLocalPart();
                                String value = input.getAttributeValue(walk);
                                recordContent.append(' ').append(attrName).append("=\"").append(value).append("\"");
                            }
                        }
                        recordContent.append(">\n");
                    }
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    if (withinRecord) {
                        if (!contentPresent && !input.getText().trim().isEmpty()) {
                            contentPresent = true;
                            recordContent.append(input.getText());
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (withinRecord) {
                        if (input.getName().equals(recordRoot) && depth == recordDepth) {
                            withinRecord = false;
                            // todo: unique!
                            // todo: lastModified
                            object = new BasicDBObject();
                            object.put(metadataFormat, recordContent.toString());
                            recordContent.setLength(0);
                        }
                        else {
                            if (contentPresent) {
                                recordContent.append('\n');
                            }
                            recordContent.append("</").append(input.getPrefixedName()).append(">\n");
                            contentPresent = false;
                        }
                    }
                    depth--;
                    break;
                case XMLEvent.END_DOCUMENT: {
                    break;
                }
            }
            if (!input.hasNext()) {
                inputStream.close();
                break;
            }
            input.next();
        }
        return object;
    }

    public void close() {
        try {
            input.close();
        }
        catch (XMLStreamException e) {
            e.printStackTrace(); // should never happen
        }
    }
}
