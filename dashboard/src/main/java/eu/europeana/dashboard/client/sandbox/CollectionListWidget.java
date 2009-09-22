package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.FilterChoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Hold a list of collections and checkboxes to make a selection from them
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionListWidget extends DashboardWidget {
    private List<EuropeanaCollectionX> collections;
    private Map<FilterChoice, CheckBox> importFileState = new HashMap<FilterChoice, CheckBox>();
    private Map<FilterChoice, CheckBox> collectionState = new HashMap<FilterChoice, CheckBox>();
    private Map<FilterChoice, CheckBox> cacheState = new HashMap<FilterChoice, CheckBox>();
    private ClickListener clickListener;

    public CollectionListWidget(World world) {
        super(world);
        String [] titles = {
                world.messages().filterNotStarted(),
                world.messages().filterInProgress(),
                world.messages().filterCompleted(),
                world.messages().filterError()
        };
        for (FilterChoice fc : FilterChoice.values()) {
            importFileState.put(fc, new CheckBox(titles[fc.ordinal()]));
        }
        for (FilterChoice fc : FilterChoice.values()) {
            collectionState.put(fc, new CheckBox(titles[fc.ordinal()]));
        }
        for (FilterChoice fc : FilterChoice.values()) {
            cacheState.put(fc, new CheckBox(titles[fc.ordinal()]));
        }
    }

    public void setCollections(List<EuropeanaCollectionX> collections) {
        this.collections = collections;
    }

    public void removeCollection(String name) {
        Iterator<EuropeanaCollectionX> walk = collections.iterator();
        while (walk.hasNext()) {
            EuropeanaCollectionX collection = walk.next();
            if (collection.getName().equals(name)) {
                walk.remove();
                break;
            }
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    protected Widget createWidget() {
        StackPanel stack = new StackPanel();
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(8);
        for (FilterChoice fc : FilterChoice.values()) {
            CheckBox checkBox = importFileState.get(fc);
            checkBox.addClickListener(clickListener);
            panel.add(checkBox);
        }
        stack.add(panel, world.messages().importFileStateTitle());
        panel = new VerticalPanel();
        panel.setSpacing(8);
        for (FilterChoice fc : FilterChoice.values()) {
            CheckBox checkBox = collectionState.get(fc);
            checkBox.addClickListener(clickListener);
            panel.add(checkBox);
        }
        stack.add(panel, world.messages().collectionStateTitle());
        panel = new VerticalPanel();
        panel.setSpacing(8);
        for (FilterChoice fc : FilterChoice.values()) {
            CheckBox checkBox = cacheState.get(fc);
            checkBox.addClickListener(clickListener);
            panel.add(checkBox);
        }
        stack.add(panel, world.messages().cacheStateTitle());
        HTML selectAll = new HTML(world.messages().selectAll());
        selectAll.setStyleName("actionLink");
        selectAll.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                setAllChecked(true);
            }
        });
        selectAll.addClickListener(clickListener);
        HTML selectNone = new HTML(world.messages().selectNone());
        selectNone.setStyleName("actionLink");
        selectNone.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                setAllChecked(false);
            }
        });
        selectNone.addClickListener(clickListener);
        HorizontalPanel bp = new HorizontalPanel();
        bp.setSpacing(5);
        bp.add(selectAll);
        bp.add(selectNone);
        panel = new VerticalPanel();
        panel.add(stack);
        panel.add(bp);
        return panel;
    }

    private void setAllChecked(boolean checked) {
        for (CheckBox box : importFileState.values()) {
            box.setChecked(checked);
        }
        for (CheckBox box : collectionState.values()) {
            box.setChecked(checked);
        }
        for (CheckBox box : cacheState.values()) {
            box.setChecked(checked);
        }
    }

    public List<EuropeanaCollectionX> getFilteredCollections() {
        List<EuropeanaCollectionX> list = new ArrayList<EuropeanaCollectionX>();
        for (EuropeanaCollectionX collection : collections) {
            if (
                    collectionState.get(collection.getCollectionState().getFilterChoice()).isChecked() ||
                    cacheState.get(collection.getCacheState().getFilterChoice()).isChecked() ||
                    importFileState.get(collection.getFileState().getFilterChoice()).isChecked()
            ) {
                list.add(collection);
            }
        }
        return list;
    }

    public EuropeanaCollectionX findCollectionName(String collectionName) {
        for (EuropeanaCollectionX collection : collections) {
            if (collection.getName().equals(collectionName)) {
                return collection;
            }
        }
        return null;
    }

    public EuropeanaCollectionX findFileName(String fileName) {
        for (EuropeanaCollectionX collection : collections) {
            if (fileName.equals(collection.getName())) {
                return collection;
            }
        }
        return null;
    }

    public SuggestOracle getSuggestOracle() {
        return new CollectionOracle();
    }

    public void addCollection(EuropeanaCollectionX collection) {
        collections.add(collection);
    }

    public int getCollectionCount() {
        return collections.size();
    }

    public static class CollectionSuggestion implements SuggestOracle.Suggestion {
        private EuropeanaCollectionX collection;

        private CollectionSuggestion(EuropeanaCollectionX collection) {
            this.collection = collection;
        }

        public EuropeanaCollectionX getCollection() {
            return collection;
        }

        public String getDisplayString() {
            return collection.getName();
        }

        public String getReplacementString() {
            return collection.getName();
        }
    }

    private class CollectionOracle extends SuggestOracle {
        @Override
        public void requestSuggestions(final Request request, final Callback callback) {
            String query = request.getQuery().toLowerCase();
            List<Suggestion> suggestions = new ArrayList<Suggestion>();
            for (EuropeanaCollectionX collection : collections) {
                String name = collection.getName().toLowerCase();
                if (name.contains(query)) {
                    suggestions.add(new CollectionSuggestion(collection));
                }
            }
            Response response = new Response(suggestions);
            callback.onSuggestionsReady(request, response);
        }
    }


}