package eu.delving.services.indexing;

import eu.europeana.core.database.domain.EuropeanaCollection;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:23 PM
 */
public interface PmhImporter {
    EuropeanaCollection commenceImport(Long collectionId);

    EuropeanaCollection abortImport(Long collectionId);

    List<EuropeanaCollection> getActiveImports();
}
