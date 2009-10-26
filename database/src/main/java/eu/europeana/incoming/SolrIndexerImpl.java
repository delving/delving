/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.query.ESERecord;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.FullDoc;
import eu.europeana.query.QueryExpression;
import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;
import eu.europeana.query.QueryProblem;
import eu.europeana.query.RecordField;
import eu.europeana.query.ResponseType;
import eu.europeana.query.ResultModel;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for sending information to the index.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrIndexerImpl implements SolrIndexer {
    protected Logger log = Logger.getLogger(getClass());
    private HttpClient httpClient;
    protected DashboardDao dashboardDao;
    private QueryModelFactory queryModelFactory;
    private String targetUrl;
    protected int chunkSize = 100;

    public void setQueryModelFactory(QueryModelFactory queryModelFactory) {
        this.queryModelFactory = queryModelFactory;
    }

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
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
            postUpdate(xml);
            postUpdate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><commit/>");
            log.info(String.format("Delete collection %s from Solr Index", collectionName));
            return true;
        }
        catch (IOException e) {
            log.error("Unable to post delete to SOLR", e);
        }
        return false;
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

    protected void postUpdate(String xml) throws IOException {
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
            throw new IOException("HTTP Problem " + responseCode + ": " + HttpStatus.getStatusText(responseCode));
        }
    }

    protected String createAddRecordsXML(List<Record> recordList) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<add>\n");
        for (Record record : recordList) {
            out.append("\t<doc>\n");
            if (record == null) {
                throw new IllegalStateException("Cannot find record for URI "+record.getEuropeanaId().getEuropeanaUri());
            }
            appendField(out, RecordField.EUROPEANA_URI, record.getEuropeanaId().getEuropeanaUri());
            appendField(out, RecordField.EUROPEANA_COLLECTION_NAME, record.getEuropeanaId().getCollection().getName());
            for (ESERecord.Field field : record.getEseRecord()) {
                if (field.getKey().getFacetType() != null) {
                    out.append("\t\t<field name=\"").append(field.getKey().getFacetType()).append("\">");
                }
                else {
                    out.append("\t\t<field name=\"").append(field.getKey().toFieldNameString()).append("\">");
                }
                out.append(field.getValue());
                out.append("</field>\n");
            }
            for (SocialTag socialTag : record.getEuropeanaId().getSocialTags()) {
                appendField(out, RecordField.EUROPEANA_USER_TAG, socialTag.getTag());
            }
            for (EditorPick editorPick : record.getEuropeanaId().getEditorPicks()) {
                appendField(out, RecordField.EUROPEANA_EDITORS_PICK, editorPick.getQuery());
            }
            out.append("\t</doc>\n");
        }
        out.append("</add>\n");
        return out.toString();
    }

    private void appendField(StringBuilder out, RecordField recordField, String value) {
        out.append("\t\t<field name=\"").append(recordField.toFieldNameString()).append("\">");
        out.append(value);
        out.append("</field>\n");
    }

    protected ESERecord fetchRecordFromSolr(String uri) throws EuropeanaQueryException {
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

        public Type getType() {
            return Type.MORE_LIKE_THIS_QUERY;
        }

        public boolean isMoreLikeThis() {
            return true;
        }
    }
}