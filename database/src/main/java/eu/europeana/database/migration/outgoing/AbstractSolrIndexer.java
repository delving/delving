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

package eu.europeana.database.migration.outgoing;

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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 25, 2009: 7:45:19 AM
 */
public class AbstractSolrIndexer implements SolrIndexer {
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

    public boolean index(List<EuropeanaId> ids, Map<String, ESERecord> records) {
        try {
            String xml = createAddRecordsXML(ids, records);
            postUpdate(xml);
            return true;
        }
        catch (Exception e) {
            log.error("Problem posting record", e);
        }
        return false;
    }

    public boolean reindex(EuropeanaId europeanaId) {
        try {
            List<EuropeanaId> list = new ArrayList<EuropeanaId>();
            list.add(europeanaId);
            Map<String, ESERecord> records = new TreeMap<String, ESERecord>();
            ESERecord record = fetchRecordFromSolr(europeanaId.getEuropeanaUri());
            records.put(europeanaId.getEuropeanaUri(), record);
            String xml = createAddRecordsXML(list, records);
            postUpdate(xml);
            return true;
        }
        catch (Exception e) {
            log.error("Problem posting record", e);
        }
        return false;
    }

    public boolean delete(String collectionName) {
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

    protected String createAddRecordsXML(List<EuropeanaId> ids, Map<String,ESERecord> records) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<add>\n");
        for (EuropeanaId id : ids) {
            out.append("\t<doc>\n");
            ESERecord record = records.get(id.getEuropeanaUri());
            if (record == null) {
                throw new IllegalStateException("Cannot find record for URI "+id.getEuropeanaUri());
            }
            for (ESERecord.Field field : record) {
                out.append("\t\t<field name=\"").append(field.getKey().toFieldNameString()).append("\">");
                out.append(field.getValue());
                out.append("</field>\n");
            }
            for (SocialTag socialTag : id.getSocialTags()) {
                out.append("\t\t<field name=\"").append(RecordField.EUROPEANA_USER_TAG.toFieldNameString()).append("\">");
                out.append(socialTag.getTag());
                out.append("</field>\n");
            }
            for (EditorPick editorPick : id.getEditorPicks()) {
                out.append("\t\t<field name=\"").append(RecordField.EUROPEANA_EDITORS_PICK.toFieldNameString()).append("\">");
                out.append(editorPick.getQuery());
                out.append("</field>\n");
            }
            out.append("\t</doc>\n");
        }
        out.append("</add>\n");
        return out.toString();
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