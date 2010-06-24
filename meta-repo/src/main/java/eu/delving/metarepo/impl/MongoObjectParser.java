package eu.delving.metarepo.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import eu.delving.metarepo.core.MetaRepo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parse XML to produce DBObject instances
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MongoObjectParser {
    private XMLStreamReader2 input;
    private QName recordRoot, uniqueElement;
    private DBObject namespaces = new BasicDBObject();

    public MongoObjectParser(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException {
        this.recordRoot = recordRoot;
        this.uniqueElement = uniqueElement;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader(getClass().getName(), inputStream);
    }

    @SuppressWarnings("unchecked")
    public synchronized DBObject nextRecord() throws XMLStreamException, IOException {
        DBObject record = null;
        StringBuilder contentBuffer = new StringBuilder();
        StringBuilder uniqueBuffer = null;
        String uniqueContent = null;
        boolean withinRecord = false;
        int depth = 0;
        int recordDepth = 0;
        boolean contentPresent = false;
        while (record == null) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    break;
                case XMLEvent.NAMESPACE:
                    System.out.println("namespace: " + input.getName());
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
                        if (input.getName().equals(uniqueElement)) {
                            uniqueBuffer = new StringBuilder();
                        }
                        String prefix = input.getPrefix();
                        String uri = input.getNamespaceURI();
                        if (prefix != null) {
                            namespaces.put(prefix, uri);
                        }
                        else {
                            throw new XMLStreamException("Unexpected null prefix for namespace uri: "+uri);
                        }
                        contentBuffer.append("<").append(input.getPrefixedName());
                        if (input.getAttributeCount() > 0) {
                            for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                QName qName = input.getAttributeName(walk);
                                String attrName = qName.getPrefix().isEmpty() ? qName.getLocalPart() : qName.getPrefix() + ":" + qName.getLocalPart();
                                String value = input.getAttributeValue(walk);
                                contentBuffer.append(' ').append(attrName).append("=\"").append(value).append("\"");
                            }
                        }
                        contentBuffer.append(">");
                    }
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    if (withinRecord) {
                        if (!contentPresent) {
                            contentPresent = !input.getText().trim().isEmpty();
                        }
                        if (contentPresent) {
                            contentBuffer.append(input.getText());
                            if (uniqueBuffer != null) {
                                uniqueBuffer.append(input.getText());
                            }
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (withinRecord) {
                        if (input.getName().equals(recordRoot) && depth == recordDepth) {
                            withinRecord = false;
                            record = new BasicDBObject();
                            record.put(MetaRepo.Record.ORIGINAL, contentBuffer.toString());
                            if (uniqueContent != null) {
                                record.put(MetaRepo.Record.UNIQUE, uniqueContent);
                            }
                            contentBuffer.setLength(0);
                        }
                        else {
                            if (contentPresent) {
                                if (uniqueBuffer != null) {
                                    String unique = uniqueBuffer.toString().trim();
                                    if (!unique.isEmpty()) {
                                        uniqueContent = unique;
                                    }
                                    uniqueBuffer = null;
                                }
                            }
                            contentBuffer.append("</").append(input.getPrefixedName()).append(">\n");
                            contentPresent = false;
                        }
                    }
                    depth--;
                    break;
                case XMLEvent.END_DOCUMENT:
                    break;
            }
            if (!input.hasNext()) {
                break;
            }
            input.next();
        }
        return record;
    }

    public DBObject getNamespaces() {
        return namespaces;
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
