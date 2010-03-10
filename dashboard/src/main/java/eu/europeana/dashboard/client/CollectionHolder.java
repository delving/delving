package eu.europeana.dashboard.client;

import com.google.gwt.core.client.GWT;
import eu.europeana.dashboard.client.dto.CollectionStateX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold a collection and a
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionHolder {
    private DashboardServiceAsync service;
    private CollectionAddListener collectionAddListener;
    private List<CollectionUpdateListener> listeners = new ArrayList<CollectionUpdateListener>();
    private EuropeanaCollectionX collection;
    private ImportFileX importFile;

    public CollectionHolder(DashboardServiceAsync service, CollectionAddListener collectionAddListener, EuropeanaCollectionX collection) {
        this.service = service;
        this.collectionAddListener = collectionAddListener;
        this.collection = collection;
    }

    public void addListener(CollectionUpdateListener listener) {
        listeners.add(listener);
    }

    public EuropeanaCollectionX getCollection() {
        return collection;
    }

    public void setCollection(EuropeanaCollectionX collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Cannot set collection to null");
        }
        if (collectionAddListener != null && this.collection.getId() == null) {
            collectionAddListener.addCollection(collection);
        }
        this.collection = collection;
        for (CollectionUpdateListener listener : listeners) {
            listener.collectionUpdated(collection);
        }
    }

    public ImportFileX getImportFile() {
        return importFile;
    }

    public void setImportFile(final ImportFileX importFile) {
        this.importFile = importFile;
        service.fetchCollection(collection.getName(), importFile.getFileName(), false, new Reply<EuropeanaCollectionX>() {
            @Override
            public void onSuccess(EuropeanaCollectionX result) {
                GWT.log("CollectionHolder: fetchCollection success " + result, null);
                result.setFileState(importFile.getState());
                result.setFileName(importFile.getFileName());
                collection = result;
                updateCollection();
            }
        });
    }

    public void setImportFileName(String fileName) {
        this.importFile = null;
        collection.setFileName(fileName);
        collection.setFileState(ImportFileX.State.NONEXISTENT);
        updateCollection();
    }

    public void setCollectionState(CollectionStateX state) {
        collection.setCollectionState(state);
        updateCollection();
    }

    public void clearImportFile() {
        this.importFile = null;
        collection.setFileName(null);
        updateCollection();
    }

    private void updateCollection() {
        service.updateCollection(collection, new Reply<EuropeanaCollectionX>() {
            @Override
            public void onSuccess(EuropeanaCollectionX collectionX) {
                setCollection(collectionX);
            }
        });
    }

    public interface CollectionUpdateListener {
        void collectionUpdated(EuropeanaCollectionX collection);
    }

    public interface CollectionAddListener {
        void addCollection(EuropeanaCollectionX collection);
    }

}
