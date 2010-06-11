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
    private int recordCount;
    private Listener listener;

    public interface Listener {
        void recordsParsed(int count, boolean lastRecord);
    }

    public DBObjectParser(InputStream inputStream, QName recordRoot, Listener listener) throws XMLStreamException {
        this.inputStream = inputStream;
        this.recordRoot = recordRoot;
        this.listener = listener;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader("Normalization", inputStream);
    }

    @SuppressWarnings("unchecked")
    public synchronized DBObject nextRecord() throws XMLStreamException, IOException {
        DBObject object = null;
        DBObject rootNode = null;
        Stack<DBObject> nodeStack = new Stack<DBObject>();
        StringBuilder value = new StringBuilder();
        boolean withinRecord = false;
        while (object == null) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    if (listener != null) {
                        listener.recordsParsed(0, false);
                    }
                    break;
                case XMLEvent.START_ELEMENT:
                    if (input.getName().equals(recordRoot)) {
                        withinRecord = true;
                    }
                    if (withinRecord) {
                        DBObject parent;
                        if (nodeStack.isEmpty()) {
                            parent = null;
                        }
                        else {
                            parent = nodeStack.peek();
                        }
                        String nodeName;
                        if (null == input.getPrefix()) {
                            nodeName = input.getName().equals(recordRoot) ? "input" : input.getLocalName();
                        }
                        else {
                            nodeName = input.getName().equals(recordRoot) ? "input" : input.getPrefix() + "_" + input.getLocalName();
                        }
                        DBObject node = new BasicDBList(); // todo: used to use parent, nodeName
                        if (input.getAttributeCount() > 0) {
                            for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                QName attributeName = input.getAttributeName(walk);
//                                node.attributes().put(attributeName.getLocalPart(), input.getAttributeValue(walk)); todo
                            }
                        }
                        nodeStack.push(node);
                        if (parent == null) {
                            rootNode = node;
                        }
                        value.setLength(0);
                    }
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    if (withinRecord) {
                        value.append(input.getText());
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (input.getName().equals(recordRoot)) {
                        withinRecord = false;
                        recordCount++;
                        object = new BasicDBObject();
                        if (listener != null) {
                            listener.recordsParsed(recordCount, false);
                        }
                    }
                    if (withinRecord) {
                        DBObject node = nodeStack.pop();
                        String valueString = value.toString().replaceAll("\n", " ").replaceAll(" +", " ").trim();
                        value.setLength(0);
                        if (valueString.length() > 0) {
//                            node.setValue(valueString); todo
                        }
                    }
                    break;
                case XMLEvent.END_DOCUMENT: {
                    break;
                }
            }
            if (!input.hasNext()) {
                inputStream.close();
                if (listener != null) {
                    listener.recordsParsed(recordCount, true);
                }
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
