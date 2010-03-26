package eu.europeana.sip.xml;

import org.apache.log4j.Logger;
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
 * Iterate through the xml file, producing groovy nodes.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class NormalizationParser {
    private Logger logger = Logger.getLogger(getClass());
    private InputStream inputStream;
    private XMLStreamReader2 input;
    private QName recordRoot;

    public NormalizationParser(InputStream inputStream, QName recordRoot) throws XMLStreamException {
        this.inputStream = inputStream;
        this.recordRoot = recordRoot;
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        this.input = (XMLStreamReader2) xmlif.createXMLStreamReader("Normalization", inputStream);
    }

    @SuppressWarnings("unchecked")
    public GroovyNode nextRecord() throws XMLStreamException, IOException {
        GroovyNode rootNode = null;
        Stack<GroovyNode> nodeStack = new Stack<GroovyNode>();
        StringBuilder value = new StringBuilder();
        boolean withinRecord = false;
        boolean finishedRecord = false;
        while (!finishedRecord) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    logger.info("Starting document");
                    break;
                case XMLEvent.START_ELEMENT:
                    if (input.getName().equals(recordRoot)) {
                        withinRecord = true;
                    }
                    if (withinRecord) {
                        GroovyNode parent;
                        if (nodeStack.isEmpty()) {
                            parent = null;
                        }
                        else {
                            parent = nodeStack.peek();
                        }
                        String nodeName = input.getName().equals(recordRoot) ? "input" : input.getPrefix()+"_"+input.getLocalName();
                        GroovyNode node = new GroovyNode(parent, nodeName);
                        if (input.getAttributeCount() > 0) {
                            for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                QName attributeName = input.getAttributeName(walk);
                                node.attributes().put(attributeName.getLocalPart(), input.getAttributeValue(walk));
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
                        finishedRecord = true;
                    }
                    if (withinRecord) {
                        GroovyNode node = nodeStack.pop();
                        if (value.length() > 0) {
                            node.setValue(value.toString());
                        }
                    }
                    break;
                case XMLEvent.END_DOCUMENT: {
                    logger.info("Ending document");
                    break;
                }
            }
            if (!input.hasNext()) {
                inputStream.close();
                break;
            }
            input.next();
        }
        return rootNode;
    }

    public void close() {
        try {
            input.close();
        }
        catch (XMLStreamException e) {
            logger.error("closing", e);
        }
    }
}