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

package eu.europeana.bootstrap;

import eu.europeana.database.migration.DataMigration;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 29, 2009: 4:15:22 PM
 */
public class LoadContent {
    private static final Logger log = Logger.getLogger(LoadContent.class);

    public static void main(String[] args) throws Exception {
        // todo: use new ESEimporter for this functionality
        DataMigration migration = new DataMigration("./bootstrap/src/main/resources/");
        log.info("Start loading Static Content into the database");
        try {
            migration.importTables();
        } catch (IOException e) {
            log.error("Unable to find the import files");
        }
        log.info("Finish loading Static Content into the database");

        SolrStarter solr = new SolrStarter();
        log.info("Starting Solr Server");
        solr.start();
        // todo: remove later
//        DataImporter importer = new DataImporter();
//        log.info("Start importing test collection");
//        String collectionFileName = importer.importFile("./database/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml");
//        importer.prepareCollectionForIndexing(collectionFileName);
//        importer.runIndexer();
        log.info("Finished importing and indexing test collection");
        Thread.sleep(10000);
        solr.stop();
        log.info("Stopping Solr server");
    }
}
