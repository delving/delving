package eu.europeana.dashboard.server.incoming;

import eu.europeana.dashboard.client.dto.ImportFile;

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
}
