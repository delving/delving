package eu.europeana.core.database.incoming;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;

/**
 * This interface defines programmatic control of the importer
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface ESEImporter {
    ImportRepository getImportRepository();
    ImportFile commenceValidate(ImportFile importFile, Long collectionId);
    ImportFile commenceImport(ImportFile importFile, Long collectionId);
    ImportFile abortImport(ImportFile importFile);
    List<ImportFile> getActiveImports();
    void commit() throws IOException, SolrServerException;
}
