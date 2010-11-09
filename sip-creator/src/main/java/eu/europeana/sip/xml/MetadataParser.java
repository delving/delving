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

import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.Tag;
import eu.europeana.sip.core.GroovyNode;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.Sanitizer;
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

public class MetadataParser {
    private InputStream inputStream;
    private XMLStreamReader2 input;
    private Path recordRoot;
    private int recordCount;
    private Path path = new Path();
    private Listener listener;

    public interface Listener {
        void recordsParsed(int count, boolean lastRecord);
    }

    public MetadataParser(InputStream inputStream, Path recordRoot, Listener listener) throws XMLStreamException {
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
    public synchronized MetadataRecord nextRecord() throws XMLStreamException, IOException {
        MetadataRecord metadataRecord = null;
        GroovyNode rootNode = null;
        Stack<GroovyNode> nodeStack = new Stack<GroovyNode>();
        StringBuilder value = new StringBuilder();
        boolean withinRecord = false;
        while (metadataRecord == null) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    if (listener != null) {
                        listener.recordsParsed(0, false);
                    }
                    break;
                case XMLEvent.START_ELEMENT:
                    path.push(Tag.create(input.getName().getPrefix(), input.getName().getLocalPart()));
                    if (path.equals(recordRoot)) {
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
                            nodeName = path.equals(recordRoot) ? "input" : Sanitizer.tagToVariable(input.getLocalName());
                        }
                        else {
                            nodeName = path.equals(recordRoot) ? "input" : input.getPrefix() + "_" + Sanitizer.tagToVariable(input.getLocalName());
                        }
                        GroovyNode node = new GroovyNode(parent, nodeName);
                        if (input.getAttributeCount() > 0) { // todo: sometimes java.lang.IllegalStateException: Current state not START_ELEMENT        
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
                    if (path.equals(recordRoot)) {
                        withinRecord = false;
                        recordCount++;
                        metadataRecord = new MetadataRecord(rootNode, recordCount);
                        if (listener != null) {
                            listener.recordsParsed(recordCount, false);
                        }
                    }
                    if (withinRecord) {
                        GroovyNode node = nodeStack.pop();
                        String valueString = value.toString().replaceAll("\n", " ").replaceAll(" +", " ").trim();
                        value.setLength(0);
                        if (valueString.length() > 0) {
                            node.setValue(valueString);
                        }
                    }
                    path.pop();
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
        return metadataRecord;
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