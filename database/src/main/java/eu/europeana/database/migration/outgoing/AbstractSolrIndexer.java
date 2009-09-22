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
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.query.RecordField;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 25, 2009: 7:45:19 AM
 */
public class AbstractSolrIndexer implements SolrIndexer {
    protected Logger log = Logger.getLogger(getClass());
    private HttpClient httpClient;
    protected DashboardDao dashboardDao;
    private String targetUrl;
    protected int chunkSize = 100;

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

    public AbstractSolrIndexer() {
//         ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
//                "/database-application-context.xml",
//                 "/test-application-context.xml"
//        });
//        dashboardDao = (DashboardDao) context.getBean("dashboardDao");
    }

    public boolean reindex(EuropeanaId europeanaId) {
        try {
            List<EuropeanaId> list = new ArrayList<EuropeanaId>();
            list.add(europeanaId);
            String xml = createAddRecordsXML(list);
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

    protected String createAddRecordsXML(List<EuropeanaId> ids) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<add>\n");
        for (EuropeanaId id : ids) {
            if (id.getBoostFactor() == null) {
                out.append("\t<doc>\n");
            }
            else {
                out.append("\t<doc boost=\"").append(id.getBoostFactor()).append("\">\n");
            }
            out.append("\t\t<field name=\"europeana_uri\">");
            out.append(id.getEuropeanaUri());
            out.append("</field>\n");
            StringTokenizer tok = new StringTokenizer(id.getSolrRecords(), "\n");
            while (tok.hasMoreElements()) {
                String line = tok.nextToken();
                if (line.indexOf("field") > 0) {
                    out.append('\t').append(line).append('\n');
                }
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
            out.append("\t\t<field name=\"").append(RecordField.EUROPEANA_COLLECTION_NAME.toFieldNameString()).append("\">");
            out.append(id.getCollection().getName());
            out.append("</field>\n");
            out.append("\t</doc>\n");
        }
        out.append("</add>\n");
        return out.toString();
    }

}