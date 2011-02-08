/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */


package eu.delving.sip;
/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

import com.ctc.wstx.stax.WstxInputFactory;
import eu.delving.metadata.Path;
import eu.delving.metadata.Tag;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

public class Harvester {
    private Logger log = Logger.getLogger(getClass());
    private Executor executor = Executors.newSingleThreadExecutor();
    private HttpClient httpClient = new HttpClient();
    private XMLInputFactory inputFactory = new WstxInputFactory();
    private List<Engine> engines = new CopyOnWriteArrayList<Engine>();

    public interface Harvest {
        String getUrl();

        String getMetadataPrefix();

        String getSpec();

        OutputStream getOutputStream();

        void failure(Exception e);
    }

    public void perform(Harvest harvest) {
        Engine engine = new Engine(harvest);
        engines.add(engine);
        executor.execute(engine);
    }

    public List<Harvest> getActive() {
        List<Harvest> active = new ArrayList<Harvest>();
        for (Engine engine : engines) {
            active.add(engine.harvest);
        }
        return active;
    }

    public class Engine implements Runnable {
        private Harvest harvest;

        public Engine(Harvest harvest) {
            this.harvest = harvest;
        }

        @Override
        public void run() {
            log.info("Harvesting " + harvest.getUrl());
            try {
                HttpMethod method = new GetMethod(String.format(
                        "%s?verb=ListRecords&metadataPrefix=%s&set=%s",
                        harvest.getUrl(),
                        harvest.getMetadataPrefix(),
                        harvest.getSpec()
                ));
                httpClient.executeMethod(method);
                InputStream inputStream = method.getResponseBodyAsStream();
                String resumptionToken = harvestXML(inputStream);
                while (!resumptionToken.isEmpty()) {
                    method = new GetMethod(String.format(
                            "%s?verb=ListRecords&resumptionToken=%s",
                            harvest.getUrl(),
                            resumptionToken
                    ));
                    httpClient.executeMethod(method);
                    inputStream = method.getResponseBodyAsStream();
                    resumptionToken = harvestXML(inputStream);
                }
                log.info("Finished harvest of " + harvest.getUrl());
            }
            catch (Exception e) {
                log.warn("Problem harvesting " + harvest.getUrl(), e);
                log.warn("Exception: " + exceptionToErrorString(e));
            }
            finally {
                engines.remove(this);
            }
        }

        private String harvestXML(InputStream inputStream) throws TransformerException, XMLStreamException, IOException, HarvestException {
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inputFactory.createXMLStreamReader(source);
            String resumptionToken = "";
            int recordCount = 0;
            boolean isInMetadataBlock = false;
            long startTime = System.currentTimeMillis();
            Path path = new Path();
            while (true) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (isErrorElement(xml)) {
                            throw new HarvestException(xml.getElementText());
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = true;
                        }
                        else if (isResumptionToken(xml)) {
                            resumptionToken = xml.getElementText();
                        }
                        else if (isRecordElement(xml) && isInMetadataBlock) {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                        }
                        else {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                            String text = xml.getElementText();
                            if (xml.isEndElement()) {
                                path.pop();
                            }
                        }
                        break;

                    case XMLEvent.CHARACTERS:
                    case XMLEvent.CDATA:
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (isRecordElement(xml) && isInMetadataBlock) {
                            isInMetadataBlock = false;
                            if (recordCount > 0 && recordCount % 500 == 0) {
                                log.info(String.format("imported %d records in %s", recordCount, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)));
                            }
                            recordCount++;
                            path.pop();
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = false;
                        }
                        path.pop();
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        log.info(String.format("Document ended, fetched %d records", recordCount));
                        break;
                }
                if (!xml.hasNext()) {
                    break;
                }
                xml.next();
            }
            inputStream.close();
            return resumptionToken;
        }

        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

        private boolean isMetadataElement(XMLStreamReader xml) {
            return "metadata".equals(xml.getName().getLocalPart());
        }

        private boolean isErrorElement(XMLStreamReader xml) {
            return "error".equals(xml.getName().getLocalPart());
        }

        private boolean isResumptionToken(XMLStreamReader xml) {
            return "resumptionToken".equals(xml.getName().getLocalPart());
        }
    }

    private static String exceptionToErrorString(Exception exception) {
        StringBuilder out = new StringBuilder();
        out.append(exception.getMessage());
        Throwable cause = exception.getCause();
        while (cause != null) {
            out.append('\n');
            out.append(cause.toString());
            cause = cause.getCause();
        }
        return out.toString();
    }

    public class HarvestException extends Exception {
        public HarvestException(String s) {
            super(s);
        }
    }
}
