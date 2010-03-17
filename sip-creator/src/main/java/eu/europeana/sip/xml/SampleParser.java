package eu.europeana.sip.xml;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SampleParser {
    private Logger log = Logger.getLogger(getClass());
    private InputStream inputStream;
    private File outputFile;
    private long sizeLimit;
    private XMLInputFactory inFactory;
    private XMLOutputFactory outFactory;

    public SampleParser(InputStream inputStream, File outputFile, long sizeLimit) {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
        this.inFactory = XMLInputFactory2.newInstance();
        this.outFactory = XMLOutputFactory2.newInstance();
        this.inputStream = inputStream;
        this.outputFile = outputFile;
        this.sizeLimit = sizeLimit;
        this.outputFile.delete();
    }

    public void run() throws TransformerException, XMLStreamException, IOException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        XMLStreamReader in = inFactory.createXMLStreamReader(inputStream);//remove , "UTF-8"
        XMLStreamWriter out = outFactory.createXMLStreamWriter(outputStream);
        int depth = 0;
        loop: while (true) {
            switch (in.getEventType()) {
                                                                                                                                       
                case XMLStreamConstants.START_DOCUMENT:
                    log.info("Document started");
                    out.writeStartDocument();
                    break;

                case XMLStreamConstants.ATTRIBUTE:
                    copyAttributes(in, out);
                    break;

                case XMLStreamConstants.CDATA:
                    out.writeCData(in.getText());
                    break;

                case XMLStreamConstants.CHARACTERS:
                    out.writeCharacters(in.getText());
                    break;

                case XMLStreamConstants.COMMENT:
                    out.writeComment(in.getText());
                    break;

                case XMLStreamConstants.DTD:
                    out.writeDTD(in.getText());
                    break;

                case XMLStreamConstants.NAMESPACE:
                    copyNamespaces(in, out);
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    out.writeStartElement(in.getPrefix(), in.getLocalName(), in.getNamespaceURI());
                    copyAttributes(in, out);
                    copyNamespaces(in, out);
                    depth++;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    out.writeEndElement();
                    depth--;
                    if (outputFile.length() > sizeLimit) {
                        log.info("Ending early at size "+outputFile.length());
                        while (depth-- > 0) {
                            out.writeCharacters("\n");
                            out.writeEndElement();
                        }
                        out.writeCharacters("\n");
                        out.writeEndDocument();
                        break loop;
                    }
                    break;

                case XMLStreamConstants.SPACE:
                    out.writeCharacters(" ");
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    log.info("Document ended");
                    out.writeEndDocument();
                    break;

                default:
                    log.info("Unknown eventType = " + in.getEventType());

            }
            if (!in.hasNext()) {
                break;
            }
            in.next();
        }
        inputStream.close();
        outputStream.close();
    }

    private void copyAttributes(XMLStreamReader xmlReader, XMLStreamWriter xmlWriter) throws XMLStreamException {
        int count = xmlReader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            xmlWriter.writeAttribute(
                    xmlReader.getAttributePrefix(i),
                    xmlReader.getAttributeNamespace(i),
                    xmlReader.getAttributeLocalName(i),
                    xmlReader.getAttributeValue(i)
            );
        }
    }

    private void copyNamespaces(XMLStreamReader xmlReader, XMLStreamWriter xmlWriter) throws XMLStreamException {
        int count = xmlReader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            xmlWriter.writeNamespace(xmlReader.getNamespacePrefix(i), xmlReader.getNamespaceURI(i));
        }
    }

//    public static void main(String[] args) throws Exception {
//        if (args.length != 3) {
//            throw new Exception("Need 3 params: inputFile outputFile desiredApproximateLength");
//        }
//        File inFile = new File(args[0]);
//        File outFile = new File(args[1]);
//        long length = Long.parseLong(args[2]);
//        SampleParser parser = new SampleParser(new FileInputStream(inFile), outFile, length);
//        parser.run();
//    }
}