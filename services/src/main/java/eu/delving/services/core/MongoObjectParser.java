package eu.delving.services.core;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mongodb.DBObject;
import eu.delving.metadata.Hasher;
import eu.delving.metadata.MetadataNamespace;
import eu.delving.metadata.Path;
import eu.delving.metadata.Tag;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

import static eu.delving.core.util.MongoObject.mob;

/**
 * Parse XML to produce DBObject instances
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MongoObjectParser {
    private XMLStreamReader2 input;
    private Path recordRoot, uniqueElement;
    private String metadataPrefix;
    private Path path = new Path();
    private Path pathWithinRecord = new Path();
    private DBObject namespaces = mob();
    private Hasher hasher = new Hasher();

    public MongoObjectParser(InputStream inputStream, Path recordRoot, Path uniqueElement, String metadataPrefix, String namespaceUri) throws XMLStreamException {
        this.recordRoot = recordRoot;
        this.uniqueElement = uniqueElement;
        this.metadataPrefix = metadataPrefix;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.configureForSpeed();
        Source source = new StreamSource(inputStream, "UTF-8");
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader(source);
        for (MetadataNamespace ns : MetadataNamespace.values()) {
            this.namespaces.put(ns.getPrefix(), ns.getUri());
        }
        this.namespaces.put(metadataPrefix, namespaceUri);
    }

    public static class Record {
        DBObject mob = mob();
        Multimap<String, String> valueMap = TreeMultimap.create();

        public Record() {
        }

        public DBObject getMob() {
            return mob;
        }

        public Multimap<String, String> getValueMap() {
            return valueMap;
        }
    }

    @SuppressWarnings("unchecked")
    public Record nextRecord() throws XMLStreamException, IOException {
        Record record = null;
        StringBuilder xmlBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        StringBuilder uniqueBuffer = null;
        String uniqueContent = null;
        boolean building = true;
        while (building) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    break;
                case XMLEvent.NAMESPACE:
                    System.out.println("namespace: " + input.getName());
                    break;
                case XMLEvent.START_ELEMENT:
                    path.push(Tag.create(input.getName().getPrefix(), input.getName().getLocalPart()));
                    if (record == null && path.equals(recordRoot)) {
                        record = new Record();
                    }
                    if (record != null) {
                        pathWithinRecord.push(path.peek());
                        if (valueBuffer.length() > 0) {
                            throw new IOException("Content and subtags not permitted");
                        }
                        if (path.equals(uniqueElement)) {
                            uniqueBuffer = new StringBuilder();
                        }
                        String prefix = input.getPrefix();
                        String uri = input.getNamespaceURI();
                        if (prefix != null && !prefix.isEmpty()) {
                            namespaces.put(prefix, uri);
                        }
                        if (!path.equals(recordRoot)) {
                            xmlBuffer.append("<").append(input.getPrefixedName());
                            if (input.getAttributeCount() > 0) {
                                for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                    QName qName = input.getAttributeName(walk);
                                    String attrName = qName.getLocalPart();
                                    if (qName.getPrefix().isEmpty()) { // only accept unprefixed attributes
                                        String value = input.getAttributeValue(walk);
                                        xmlBuffer.append(' ').append(attrName).append("=\"").append(value).append("\"");
                                    }
                                }
                            }
                            xmlBuffer.append(">");
                        }
                    }
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    if (record != null) {
                        String text = input.getText();
                        if (!text.trim().isEmpty()) {
                            for (int walk = 0; walk < text.length(); walk++) { // return predeclared entities to escapes
                                char c = text.charAt(walk);
                                switch (c) {
                                    case '&':
                                        valueBuffer.append("&amp;");
                                        break;
                                    case '<':
                                        valueBuffer.append("&lt;");
                                        break;
                                    case '>':
                                        valueBuffer.append("&gt;");
                                        break;
                                    case '"':
                                        valueBuffer.append("&quot;");
                                        break;
                                    case '\'':
                                        valueBuffer.append("&apos;");
                                        break;
                                    default:
                                        valueBuffer.append(c);
                                }
                            }
                            if (uniqueBuffer != null) {
                                uniqueBuffer.append(text);
                            }
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (record != null) {
                        if (path.equals(recordRoot)) {
                            record.getMob().put(metadataPrefix, xmlBuffer.toString());
                            if (uniqueContent != null) { // todo: should it not always be there?  should we not save if it isn't?
                                record.getMob().put(MetaRepo.Record.UNIQUE, uniqueContent);
                            }
                            record.getMob().put(MetaRepo.Record.HASH, createHashToPathMap(record.getValueMap()));
                            xmlBuffer.setLength(0);
                            building = false;
                        }
                        else {
                            if (valueBuffer.length() > 0) {
                                if (uniqueBuffer != null) {
                                    String unique = uniqueBuffer.toString().trim();
                                    if (!unique.isEmpty()) {
                                        uniqueContent = unique;
                                    }
                                    uniqueBuffer = null;
                                }
                                String value = valueBuffer.toString();
                                xmlBuffer.append(value);
                                record.getValueMap().put(pathWithinRecord.toString(), value);
                            }
                            xmlBuffer.append("</").append(input.getPrefixedName()).append(">\n");
                            valueBuffer.setLength(0);
                        }
                        pathWithinRecord.pop();
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

    private Object createHashToPathMap(Multimap<String, String> valueMap) {
        DBObject mob = mob();
        for (String path : valueMap.keys()) {
            int index = 0;
            for (String value : valueMap.get(path)) {
                mob.put(hasher.getHashString(value), index == 0 ? path : String.format("%s_%d", path, index++));
                index++;
            }
        }
        return mob;
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
