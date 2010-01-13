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

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;

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
    private SolrServer solrServer;
    protected int chunkSize = 100;
    boolean httpError;

    @Autowired
    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = Integer.parseInt(chunkSize);
    }

    public boolean indexRecordList(List<Record> recordList) {
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>(recordList.size());
        try {
            solrServer.add(docs); // todo: check the returned UpdateResponse?
            solrServer.commit();
            return true;
        }
        catch (Exception e) {
            log.error("Problem posting record", e);
        }
        return false;
    }

    public boolean deleteCollectionByName(String collectionName) {
        try {
            log.info(String.format("Delete collection %s from Solr Index", collectionName));
            solrServer.deleteByQuery("europeana_collection:\""+collectionName+"\"");
            solrServer.commit();
            return true;
        }
        catch (Exception e) {
            log.error("Unable to delete collection", e);
            httpError = true;
        }
        return false;
    }

    public boolean commit() {
        try {
            log.info("solr commit sent");
            solrServer.commit();
            return true;
        }
        catch (Exception e) {
            log.error("Unable to post commit to SOLR", e);
            httpError = true;
        }
        return false;
    }

    public boolean isHttpError() {
        return httpError;
    }
}