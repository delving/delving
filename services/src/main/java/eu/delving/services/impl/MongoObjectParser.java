package eu.delving.services.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import eu.delving.core.metadata.MetadataNamespace;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.Tag;
import eu.delving.services.core.MetaRepo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Parse XML to produce DBObject instances
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MongoObjectParser {
    private XMLStreamReader2 input;
    private Path recordRoot, uniqueElement;
    private String metadataPrefix, namespaceUri;
    private Path path = new Path();
    private DBObject namespaces = new BasicDBObject();

    public MongoObjectParser(InputStream inputStream, Path recordRoot, Path uniqueElement, String metadataPrefix, String namespaceUri) throws XMLStreamException {
        this.recordRoot = recordRoot;
        this.uniqueElement = uniqueElement;
        this.metadataPrefix = metadataPrefix;
        this.namespaceUri = namespaceUri;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.configureForSpeed();
        Source source = new StreamSource(inputStream, "UTF-8");
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader(source);
        for (MetadataNamespace ns : MetadataNamespace.values()) {
            this.namespaces.put(ns.getPrefix(), ns.getUri());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized DBObject nextRecord() throws XMLStreamException, IOException {
        DBObject record = null;
        StringBuilder contentBuffer = new StringBuilder();
        StringBuilder uniqueBuffer = null;
        String uniqueContent = null;
        boolean withinRecord = false;
        boolean contentPresent = false;
        while (record == null) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    break;
                case XMLEvent.NAMESPACE:
                    System.out.println("namespace: " + input.getName());
                    break;
                case XMLEvent.START_ELEMENT:
                    path.push(Tag.create(input.getName().getPrefix(),input.getName().getLocalPart()));
                    if (!withinRecord) {
                        if (path.equals(recordRoot)) {
                            withinRecord = true;
                        }
                    }
                    else {
                        if (contentPresent) {
                            throw new IOException("Content and subtags not permitted");
                        }
                        if (path.equals(uniqueElement)) {
                            uniqueBuffer = new StringBuilder();
                        }
                        String prefix = input.getPrefix();
                        String uri = input.getNamespaceURI();
                        if (prefix != null) {
                            namespaces.put(prefix, uri);
                        }
                        else {
                            namespaces.put(metadataPrefix, namespaceUri); // todo: set it up as the default namespace
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
                            String text = input.getText();
                            for (int walk = 0; walk < text.length(); walk++) { // return predeclared entities to escapes
                                char c = text.charAt(walk);
                                switch (c) {
                                    case '&':
                                        contentBuffer.append("&amp;");
                                        break;
                                    case '<':
                                        contentBuffer.append("&lt;");
                                        break;
                                    case '>':
                                        contentBuffer.append("&gt;");
                                        break;
                                    case '"':
                                        contentBuffer.append("&quot;");
                                        break;
                                    case '\'':
                                        contentBuffer.append("&apos;");
                                        break;
                                    default:
                                        contentBuffer.append(c);
                                }
                            }
                            if (uniqueBuffer != null) {
                                uniqueBuffer.append(input.getText());
                            }
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (withinRecord) {
                        if (path.equals(recordRoot)) {
                            withinRecord = false;
                            record = new BasicDBObject();
                            String content = contentBuffer.toString();
                            record.put(metadataPrefix, content);
                            record.put(MetaRepo.Record.MODIFIED, new Date());
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
                    path.pop();
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
