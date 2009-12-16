/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.incoming;

import com.ctc.wstx.stax.WstxOutputFactory;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.query.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for sending information to the index.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrIndexerImpl implements SolrIndexer {
    private Logger log = Logger.getLogger(getClass());
    private XMLOutputFactory outFactory = new WstxOutputFactory();
    private QueryModelFactory queryModelFactory;
    private DashboardDao dashboardDao;
    private HttpClient httpClient;
    private String targetUrl;
    protected int chunkSize = 100;
    boolean httpError;

    // not @Autowired because there are multiple
    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = Integer.parseInt(chunkSize);
    }

    public boolean indexRecordList(List<Record> recordList) {
        try {
            String xml = createAddRecordsXML(recordList);
            postUpdate(xml);
            return true;
        }
        catch (Exception e) {
            log.error("Problem posting record", e);
        }
        return false;
    }

    public boolean indexSingleRecord(EuropeanaId europeanaId) {
        try {
            List<Record> recordList = new ArrayList<Record>();
            recordList.add(new Record(europeanaId, fetchRecordFromSolr(europeanaId.getEuropeanaUri())));
            String xml = createAddRecordsXML(recordList);
            postUpdate(xml);
            return true;
        }
        catch (Exception e) {
            log.error("Problem posting record", e);
        }
        return false;
    }

    public boolean deleteCollectionByName(String collectionName) {
        String xml = createDeleteRecordsXML(collectionName);
        try {
            log.info(String.format("Delete collection %s from Solr Index", collectionName));
            postUpdate(xml);
            commit();
            return true;
        }
        catch (IOException e) {
            log.error("Unable to post delete to SOLR", e);
            httpError = true;
        }
        return false;
    }

    public boolean commit() {
        try {
            log.info("solr commit sent");
            postUpdate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><commit/>");
            return true;
        }
        catch (IOException e) {
            log.error("Unable to post commit to SOLR", e);
            httpError = true;
        }
        return false;
    }

    public boolean isHttpError() {
        return httpError;
    }

    private String createDeleteRecordsXML(String collectionName) {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<delete>\n");
        out.append("\t<query>");
        out.append(RecordField.EUROPEANA_COLLECTION_NAME.toFieldNameString());
        out.append(":\"");
        out.append(collectionName);
        out.append("\"</query>\n");
        out.append("</delete>\n");
        return out.toString();
    }

    private void postUpdate(String xml) throws IOException {
        int responseCode = 0;
        PostMethod postMethod = new PostMethod(targetUrl);
        postMethod.setRequestEntity(new StringRequestEntity(xml, "text/xml", "UTF-8"));
        try {
            responseCode = httpClient.executeMethod(postMethod);
            if (responseCode == HttpStatus.SC_OK) {
                log.info("Succeeded in posting to " + targetUrl);
            }
        }
        catch (IOException e) {
            log.info("Failed to post to " + targetUrl + ". Trying next target. ");
        }
        finally {
            postMethod.releaseConnection();
        }
        if (responseCode != HttpStatus.SC_OK) {
            httpError = true;
            throw new IOException("HTTP Problem " + responseCode + ": " + HttpStatus.getStatusText(responseCode));
        }
    }

    private String createAddRecordsXML(List<Record> recordList) throws IOException, XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter out = outFactory.createXMLStreamWriter(stringWriter);
        out.writeStartDocument("UTF-8", "1.0");
        out.writeStartElement("add");
        for (Record record : recordList) {
            out.writeStartElement("doc");
            appendField(out, RecordField.EUROPEANA_URI, record.getEuropeanaId().getEuropeanaUri());
            appendField(out, RecordField.EUROPEANA_COLLECTION_NAME, record.getEuropeanaId().getCollection().getName());
            for (ESERecord.Field field : record.getEseRecord()) {
                appendField(out, field.getKey(), field.getValue());
            }
            for (SocialTag socialTag : record.getEuropeanaId().getSocialTags()) {
                appendField(out, RecordField.EUROPEANA_USER_TAG, socialTag.getTag());
            }
            out.writeEndElement();
        }
        out.writeEndElement();
        out.writeEndDocument();
        return stringWriter.toString();
    }

    private void appendField(XMLStreamWriter out, RecordField recordField, String value) throws XMLStreamException {
        if (recordField.getFacetType() != null) {
            appendField(out, recordField.getFacetType().toString(), value);
        }
        else {
            appendField(out, recordField.toFieldNameString(), value);
        }
    }

    private void appendField(XMLStreamWriter out, String name, String value) throws XMLStreamException {
        out.writeStartElement("field");
        out.writeAttribute("name", name);
        out.writeCharacters(value);
        out.writeEndElement();
    }

    private ESERecord fetchRecordFromSolr(String uri) throws EuropeanaQueryException {
        log.info("fetching record for "+uri);
        QueryModel queryModel = queryModelFactory.createQueryModel(QueryModelFactory.SearchType.SIMPLE);
        queryModel.setResponseType(ResponseType.SINGLE_FULL_DOC);
        queryModel.setQueryExpression(new UriExpression(uri));
        ResultModel resultModel = queryModel.fetchResult();
        if (resultModel.isMissingFullDoc()) {
            EuropeanaId id = dashboardDao.fetchEuropeanaId(uri);
            if (id != null && id.isOrphan()) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_REVOKED.toString());
            }
            else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_INDEXED.toString());
            }
            else {
                throw new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
            }
        }
        FullDoc fullDoc = resultModel.getFullDoc();
        return fullDoc.getESERecord();
    }

    private class UriExpression implements QueryExpression {
        private String uri;

        private UriExpression(String uri) {
            this.uri = uri;
        }

        public String getQueryString() {
            return getBackendQueryString();
        }

        public String getBackendQueryString() {
            return RecordField.EUROPEANA_URI.toFieldNameString()+":\""+uri+"\"";
        }

        public QueryType getType() {
            return QueryType.MORE_LIKE_THIS_QUERY;
        }

        public boolean isMoreLikeThis() {
            return true;
        }
    }
}