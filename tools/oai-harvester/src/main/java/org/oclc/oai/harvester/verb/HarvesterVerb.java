/**
 * Copyright 2006 OCLC, Online Computer Library Center
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oclc.oai.harvester.verb;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb {
    private static final Logger LOG = Logger.getLogger(HarvesterVerb.class);
    protected static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
    protected static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private Document doc = null;
    private String schemaLocation = null;
    private String requestURL = null;
    private HttpClient httpClient = new HttpClient();
    private DocumentBuilder builder;
    private Element namespaceElement;
    private Transformer transformer = null;

    protected HarvesterVerb() throws ParserConfigurationException, TransformerConfigurationException {
        transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        prepareNamespace();
    }

    public HarvesterVerb(String requestURL) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        this();
        harvest(requestURL);
    }

    private void prepareNamespace() {
        DOMImplementation impl = builder.getDOMImplementation();
        Document namespaceHolder = impl.createDocument(
                "http://www.oclc.org/research/software/oai/harvester",
                "harvester:namespaceHolder",
                null
        );
        namespaceElement = namespaceHolder.getDocumentElement();
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:harvester",
                "http://www.oclc.org/research/software/oai/harvester"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai20",
                "http://www.openarchives.org/OAI/2.0/"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_GetRecord",
                "http://www.openarchives.org/OAI/1.1/OAI_GetRecord"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_Identify",
                "http://www.openarchives.org/OAI/1.1/OAI_Identify"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_ListIdentifiers",
                "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_ListMetadataFormats",
                "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_ListRecords",
                "http://www.openarchives.org/OAI/1.1/OAI_ListRecords"
        );
        namespaceElement.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:oai11_ListSets",
                "http://www.openarchives.org/OAI/1.1/OAI_ListSets"
        );
    }

    public void harvest(String requestURL) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        this.requestURL = requestURL;
        LOG.info("requesting:" + requestURL);
        GetMethod getMethod = new GetMethod(requestURL);
        getMethod.setRequestHeader("User-Agent", "OAIHarvester/2.0");
        getMethod.setRequestHeader("Accept-Encoding", "compress, gzip, identify");
        int httpStatus = httpClient.executeMethod(getMethod);
        if (httpStatus != HttpStatus.SC_OK) {
            if (httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                LOG.info("Error HTTP response: " + httpStatus + " but we will try to continue...");
                return;
            }
            throw new IOException("HTTP response: " + HttpStatus.getStatusText(httpStatus));
        }
        LOG.info("received OK for:" + requestURL);
        Header contentEncodingHeader = getMethod.getResponseHeader("Content-Encoding");
        String contentEncoding = contentEncodingHeader == null ? "unknown" : contentEncodingHeader.getValue();
        LOG.debug("contentEncoding=" + contentEncoding);
        InputStream response = getMethod.getResponseBodyAsStream();
        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(response);
            zis.getNextEntry();
            response = zis;
        }
        else if ("gzip".equals(contentEncoding)) {
            response = new GZIPInputStream(response);
        }
        else if ("deflate".equals(contentEncoding)) {
            response = new InflaterInputStream(response);
        }

        doc = builder.parse(new InputSource(response));

        StringTokenizer tokenizer = new StringTokenizer(getSingleString("/*/@xsi:schemaLocation"), " ");
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(tokenizer.nextToken());
        }
        this.schemaLocation = sb.toString();
    }

    /**
     * Get the OAI response as a DOM object
     *
     * @return the DOM for the OAI response
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Get the xsi:schemaLocation for the OAI response
     *
     * @return the xsi:schemaLocation value
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    public NodeList getErrors() throws TransformerException {
        if (SCHEMA_LOCATION_V2_0.equals(getSchemaLocation())) {
            return XPathAPI.selectNodeList(getDocument(), "/oai20:OAI-PMH/oai20:error", namespaceElement);
        }
        else {
            return null;
        }
    }

    /**
     * Get the OAI request URL for this response
     *
     * @return the OAI request URL as a String
     */
    public String getRequestURL() {
        return requestURL;
    }

    public String getSingleString(String xpath) throws TransformerException {
        return getSingleString(getDocument(), xpath);
    }

    public String getSingleString(Node node, String xpath) throws TransformerException {
        return XPathAPI.eval(node, xpath, namespaceElement).str();
    }

    public String toString() {
        Source input = new DOMSource(getDocument());
        StringWriter sw = new StringWriter();
        Result output = new StreamResult(sw);
        try {
            transformer.transform(input, output);
            return sw.toString();
        }
        catch (TransformerException e) {
            return e.getMessage();
        }
    }
}
