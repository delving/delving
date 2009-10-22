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

import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.query.ESERecord;

import java.util.List;
import java.util.Map;

/**
 * An interface that lets us send commands to Solr
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface SolrIndexer {

    /**
     * Send a number of records to the index in one request.
     *
     * @param ids which ids are to be indexed
     * @param records a map, each record is a collectin of field-string mappings
     * @return true if it worked
     */
    
    boolean index(List<EuropeanaId> ids, Map<String, ESERecord> records);

    /**
     * Reindex a single europeanaId
     *
     * @param europeanaId which one to reindex
     * @return true if it worked
     */

    boolean reindex(EuropeanaId europeanaId);

    /**
     * Delete an entire collection from the index
     *
     * @param collectionName what is it called?
     * @return true if it worked
     */
    
    boolean delete(String collectionName);
}