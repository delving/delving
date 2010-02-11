package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.QueueEntryX;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simplified way of importing collections for sandbox testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SandboxWidget extends DashboardWidget {
    private static final int STATUS_CHECK_DELAY = 5000;
    private static final String ACTION_GROUP = "action";
    private CollectionListWidget collectionListWidget;
    private SuggestBox selectedCollectionBox;
    private QueuePoller queuePoller = new QueuePoller();
    private CollectionListener collectionListener = new CollectionListener();
    private Map<String, CollectionWidget> collectionWidgets = new TreeMap<String, CollectionWidget>();
    private VerticalPanel rightPanel;
    private RadioButton newDataSet;
    private RadioButton showDataSet;
    private RadioButton browseDataSets;
    private EuropeanaCollectionX selectedCollection;

    public SandboxWidget(World world) {
        super(world);
        collectionListWidget = new CollectionListWidget(world);
        collectionListWidget.setClickHandler(new ClickHandler() {
            public void onClick(ClickEvent widget) {
                browseDataSets.setValue(true);
                showFilteredCollections();
            }
        });
    }

    private void showFilteredCollections() {
        List<EuropeanaCollectionX> filtered = collectionListWidget.getFilteredCollections();
        rightPanel.clear();
        collectionWidgets.clear();
        for (EuropeanaCollectionX collection : filtered) {
            CollectionWidget collectionWidget = new CollectionWidget(world, collection, collectionListener);
            collectionWidgets.put(collection.getName(), collectionWidget);
            rightPanel.add(collectionWidget.getWidget());
        }
    }

    private void showSelectedCollection() {
        if (selectedCollection == null) {
            rightPanel.clear();
        }
        else {
            rightPanel.clear();
            collectionWidgets.clear();
            CollectionWidget collectionWidget = new CollectionWidget(SandboxWidget.this.world, selectedCollection, collectionListener);
            collectionWidgets.put(selectedCollection.getName(), collectionWidget);
            rightPanel.add(collectionWidget.getWidget());
        }
    }

    private void showNewCollection(int collectionNumber) {
        rightPanel.clear();
        collectionWidgets.clear();
        String name = world.user().getProviderId() + ((collectionNumber < 10) ? "0" : "" + (collectionNumber / 10)) + ("" + (collectionNumber % 10));
        CollectionWidget collectionWidget = new CollectionWidget(SandboxWidget.this.world, collectionListener, name);
        rightPanel.add(collectionWidget.getWidget());
    }

    protected Widget createWidget() {
        HorizontalSplitPanel split = new HorizontalSplitPanel();
        split.setSplitPosition("30%");
        split.setLeftWidget(createLeftWidget());
        split.setRightWidget(createRightWidget());
        split.setHeight("500");
        split.setWidth("100%");
        newDataSet.setEnabled(false);
        world.service().fetchCollections(world.user().getProviderId(), new Reply<List<EuropeanaCollectionX>>() {
            public void onSuccess(List<EuropeanaCollectionX> result) {
                collectionListWidget.setCollections(result);
                newDataSet.setEnabled(true);
                newDataSet.setValue(true);
                showNewCollection(result.size()+1);
                queuePoller.schedule(STATUS_CHECK_DELAY);
            }
        });
        return split;
    }

    private Widget createLeftWidget() {
        newDataSet = new RadioButton(ACTION_GROUP, world.messages().newDataSet());
        newDataSet.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent widget) {
                showNewCollection(collectionListWidget.getCollectionCount()+1);
            }
        });
        showDataSet = new RadioButton(ACTION_GROUP, world.messages().showDataSet());
        showDataSet.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent widget) {
                showSelectedCollection();
            }
        });
        selectedCollectionBox = new SuggestBox(collectionListWidget.getSuggestOracle());
        selectedCollectionBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                showDataSet.setValue(true);
                CollectionListWidget.CollectionSuggestion suggestion = (CollectionListWidget.CollectionSuggestion) event.getSelectedItem();
                selectedCollection = suggestion.getCollection();
                if (showDataSet.getValue()) {
                    showSelectedCollection();
                }
            }
        });
        VerticalPanel showDataSetPanel = new VerticalPanel();
        showDataSetPanel.add(showDataSet);
        showDataSetPanel.add(selectedCollectionBox);
        browseDataSets = new RadioButton(ACTION_GROUP, world.messages().browseDataSets());
        browseDataSets.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent widget) {
                showFilteredCollections();
            }
        });
        VerticalPanel vertical = new VerticalPanel();
        vertical.setSpacing(5);
        vertical.add(new Label(world.messages().projectId() + " " + world.user().getProjectId()));
        vertical.add(new Label(world.messages().providerId() + " " + world.user().getProviderId()));
        vertical.add(newDataSet);
        vertical.add(showDataSetPanel);
        vertical.add(browseDataSets);
        vertical.add(collectionListWidget.getWidget());
        return vertical;
    }

    private Widget createRightWidget() {
        rightPanel = new VerticalPanel();
        rightPanel.setWidth("100%");
        rightPanel.setSpacing(5);
        return rightPanel;
    }

    private class QueuePoller extends Timer {
        public void run() {
            world.service().fetchQueueEntries(new Reply<List<QueueEntryX>>() {
                public void onSuccess(List<QueueEntryX> result) {
                    Set<String> collectionsTouched = new HashSet<String>();
                    for (QueueEntryX entry : result) {
                        CollectionWidget collectionWidget = collectionWidgets.get(entry.getCollection().getName());
                        if (collectionWidget != null) {
                            GWT.log("queue poller: got " + entry, null);
                            collectionWidget.setQueueEntry(entry);
                            collectionsTouched.add(entry.getCollection().getName());
                        }
                    }
                    for (Map.Entry<String, CollectionWidget> entry : collectionWidgets.entrySet()) {
                        if (!collectionsTouched.contains(entry.getKey())) {
                            final CollectionWidget collectionWidget = entry.getValue();
                            world.service().fetchCollection(entry.getKey(), null, false, new Reply<EuropeanaCollectionX>() {
                                public void onSuccess(EuropeanaCollectionX result) {
                                    collectionWidget.setCollection(result);
                                }
                            });
                        }
                    }
                    queuePoller.schedule(STATUS_CHECK_DELAY);
                }
            });
        }
    }

    private class CollectionListener implements CollectionHolder.CollectionAddListener {
        public void addCollection(EuropeanaCollectionX collection) {
            collectionListWidget.addCollection(collection);
            selectedCollection = collection;
            selectedCollectionBox.setText(collection.getName());
            showDataSet.setValue(true);
            showSelectedCollection();
        }
    }

}
