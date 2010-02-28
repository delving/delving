package eu.europeana.dashboard.client.collections;

import com.google.gwt.user.client.ui.SuggestOracle;
import eu.europeana.dashboard.client.dto.CollectionStateX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;

import java.util.*;

/**
 * Hold a list of collections and deliver a suggestion oracle
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionList {
    private List<EuropeanaCollectionX> collections;
    private Set<String> avoidNames = new TreeSet<String>();

    public CollectionList(List<EuropeanaCollectionX> collections) {
        this.collections = collections;
    }

    public SuggestOracle getSuggestOracle() {
        return new CollectionOracle();
    }

    public List<EuropeanaCollectionX> getCollections(CollectionStateX collectionState, ImportFileX.State fileState) {
        List<EuropeanaCollectionX> list = new ArrayList<EuropeanaCollectionX>();
        for (EuropeanaCollectionX collection : collections) {
            boolean included = true;
            if (collectionState != null && collection.getCollectionState() != collectionState) {
                included = false;
            }
            if (fileState != null && collection.getFileState() != fileState) {
                included = false;
            }
            if (included) {
                list.add(collection);
            }
        }
        return list;
    }

    private EuropeanaCollectionX findByCollectionName(String collectionName) {
        for (EuropeanaCollectionX collection: collections) {
            if (collection.getName().equals(collectionName)) {
                return collection;
            }
        }
        return null;
    }

    public EuropeanaCollectionX findByFileName(String fileName) {
        for (EuropeanaCollectionX collection: collections) {
            if (collection.getFileName() != null && collection.getFileName().equals(fileName)) {
                return collection;
            }
        }
        return null;
    }

    public EuropeanaCollectionX findByImportFile(ImportFileX importFile) {
        EuropeanaCollectionX collection = findByFileName(importFile.getFileName());
        if (collection == null) {
            collection = findByCollectionName(importFile.deriveCollectionName());
        }
        return collection;
    }

    public void setAvoidance(String avoidName, boolean avoid) {
        if (avoid) {
            avoidNames.add(avoidName);
        }
        else {
            avoidNames.remove(avoidName);
        }
    }

    public void updateCollection(EuropeanaCollectionX collectionX) {
        Iterator<EuropeanaCollectionX> walk = collections.iterator();
        while (walk.hasNext()) {
            EuropeanaCollectionX collection = walk.next();
            if (collection.getName().equals(collectionX.getName())) {
                walk.remove();
                break;
            }
        }
        collections.add(collectionX);
    }

    public static class Suggestion implements SuggestOracle.Suggestion {
        private EuropeanaCollectionX collection;

        private Suggestion(EuropeanaCollectionX collection) {
            this.collection = collection;
        }

        public EuropeanaCollectionX getCollection() {
            return collection;
        }

        @Override
        public String getDisplayString() {
            return collection.getName();
        }

        @Override
        public String getReplacementString() {
            return collection.getName();
        }
    }

    private class CollectionOracle extends SuggestOracle {
        @Override
        public void requestSuggestions(final Request request, final Callback callback) {
            String query = request.getQuery().toLowerCase();
            List<CollectionList.Suggestion> suggestions = new ArrayList<CollectionList.Suggestion>();
            for (EuropeanaCollectionX collection : collections) {
                if (avoidNames.contains(collection.getName())) {
                    continue;
                }
                String name = collection.getName().toLowerCase();
                if (name.contains(query)) {
                    suggestions.add(new CollectionList.Suggestion(collection));
                }
            }
            Response response = new Response(suggestions);
            callback.onSuggestionsReady(request, response);
        }
    }

}
