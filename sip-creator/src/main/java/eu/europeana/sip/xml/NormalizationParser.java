/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

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
    private Listener listener;

    public interface Listener {
        void finished(boolean success);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

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
                        String nodeName;
                        if (null == input.getPrefix()) {
                            nodeName = input.getName().equals(recordRoot) ? "input" : input.getLocalName();
                        }
                        else {
                            nodeName = input.getName().equals(recordRoot) ? "input" : input.getPrefix() + "_" + input.getLocalName();
                        }
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
                        String valueString = value.toString().trim();
                        value.setLength(0);
                        if (valueString.length() > 0) {
                            node.setValue(valueString);
                        }
                    }
                    break;
                case XMLEvent.END_DOCUMENT: {
                    logger.info("Ending document");
                    if (null != listener) {
                        listener.finished(true);
                    }
                    break;
                }
            }
            if (!input.hasNext()) {
                inputStream.close();
                break;
            }
            input.next();
        }
        StringBuilder recordPrinted = new StringBuilder();
//        printRecord(rootNode, recordPrinted, 0);
//        logger.info("Read record :\n" + recordPrinted);
        return rootNode;
    }

    private void printRecord(GroovyNode node, StringBuilder out, int depth) {
        if (node.value() instanceof GroovyNodeList) {
            for (int walk = 0; walk < depth; walk++) {
                out.append(' ');
            }
            GroovyNodeList list = (GroovyNodeList) node.value();
            out.append(node.name()).append("\n");
            for (Object member : list) {
                GroovyNode childNode = (GroovyNode) member;
                printRecord(childNode, out, depth + 1);
            }
        }
        else {
            for (int walk = 0; walk < depth; walk++) {
                out.append(' ');
            }
            out.append(node.name()).append(" := ").append(node.value().toString()).append("\n");
        }
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