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

package eu.europeana.dashboard.client.collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.CollectionStateX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;
import eu.europeana.dashboard.client.dto.QueueEntryX;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A widget to handle collections
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionsWidget extends DashboardWidget {
    private static final int STATUS_CHECK_DELAY = 5000;
    private CollectionList collectionList;
    private VerticalPanel mainPanel = new VerticalPanel();
    private Map<String, CollectionPanel> collectionPanels = new TreeMap<String, CollectionPanel>();
    private CollectionClose collectionClose = new CollectionClose();
    private QueuePoller queuePoller = new QueuePoller();

    public CollectionsWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        mainPanel.setSpacing(10);
        mainPanel.setWidth("100%");
        mainPanel.add(new HTML(world.messages().loadingCollections()));
        world.service().fetchCollections(new Reply<List<EuropeanaCollectionX>>() {
            public void onSuccess(List<EuropeanaCollectionX> result) {
                collectionsArrived(result);
                queuePoller.schedule(STATUS_CHECK_DELAY);
            }
        });
        return mainPanel;
    }

    private void collectionsArrived(List<EuropeanaCollectionX> collections) {
        this.collectionList = new CollectionList(collections);
        mainPanel.clear();
        VerticalPanel p = new VerticalPanel();
        p.add(createFileUploadPanel());
        p.add(createCollectionSuggestBox());
        p.add(createStateSelectorPanel());
        mainPanel.add(p);
        world.service().fetchImportFiles(true, new Reply<List<ImportFileX>>() {
            public void onSuccess(List<ImportFileX> result) {
                for (ImportFileX file : result) {
                    setImportFile(file);
                }
            }
        });
    }

    private Widget createStateSelectorPanel() {
        final ListBox collectionStateBox = new ListBox();
        collectionStateBox.addItem(world.messages().anyCollectionState());
        for (CollectionStateX state : CollectionStateX.values()) {
            collectionStateBox.addItem(state.toString());
        }
        final ListBox fileStateBox = new ListBox();
        fileStateBox.addItem(world.messages().anyImportFileState());
        for (ImportFileX.State state : ImportFileX.State.values()) {
            fileStateBox.addItem(state.toString());
        }
        Button select = new Button(world.messages().select());
        select.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)  {
                int collectionIndex = collectionStateBox.getSelectedIndex();
                int fileStateIndex = fileStateBox.getSelectedIndex();
                CollectionStateX collectionState = collectionIndex == 0 ? null : CollectionStateX.values()[collectionIndex - 1];
                ImportFileX.State fileState = fileStateIndex == 0 ? null : ImportFileX.State.values()[fileStateIndex - 1];
                for (EuropeanaCollectionX collection : collectionList.getCollections(collectionState, fileState)) {
                    if (!collectionPanels.containsKey(collection.getName())) {
                        addCollectionPanel(collection);
                    }
                }
            }
        });
        HorizontalPanel p = new HorizontalPanel();
        p.setSpacing(4);
        p.add(collectionStateBox);
        p.add(fileStateBox);
        p.add(select);
        return p;
    }

    private Widget createFileUploadPanel() {
        FileUploadPanel panel = new FileUploadPanel(world);
        panel.setNotifier(new FileUploadPanel.Status() {
            public void uploadStarted(String fileName) {
                setImportFileName(fileName);
            }

            public void uploadEnded(String fileName) {
                waitForFile(fileName);
            }
        });
        return panel.createWidget();
    }

    private void setImportFileName(final String importFileName) {
        ImportFileX importFile = new ImportFileX(importFileName, ImportFileX.State.NONEXISTENT);
        EuropeanaCollectionX collection = collectionList.findByImportFile(importFile);
        if (collection != null) {
            CollectionPanel cp = collectionPanels.get(collection.getName());
            if (cp == null) {
                cp = addCollectionPanel(collection);
            }
            cp.setImportFileName(importFileName);
        }
        else {
            collection = createNewCollection(importFile);
            world.service().updateCollection(collection, new Reply<EuropeanaCollectionX>() {
                public void onSuccess(EuropeanaCollectionX result) {
                    addCollectionPanel(result);
                }
            });
        }
    }

    private void waitForFile(final String importFileName) {
        EuropeanaCollectionX collection = collectionList.findByFileName(importFileName);
        if (collection != null) {
            CollectionPanel cp = collectionPanels.get(collection.getName());
            if (cp == null) {
                cp = addCollectionPanel(collection);
            }
            cp.waitForFile();
        }
    }

    private void setImportFile(final ImportFileX importFile) {
        EuropeanaCollectionX collection = collectionList.findByImportFile(importFile);
        if (collection != null) {
            CollectionPanel cp = collectionPanels.get(collection.getName());
            if (cp == null) {
                cp = addCollectionPanel(collection);
                cp.setImportFile(importFile);
            }
        }
        else {
            CollectionPanel cp = collectionPanels.get(importFile.deriveCollectionName());
            if (cp == null) {
                collection = createNewCollection(importFile);
                world.service().updateCollection(collection, new Reply<EuropeanaCollectionX>() {
                    public void onSuccess(EuropeanaCollectionX collectionX) {
                        addCollectionPanel(collectionX);
                    }
                });
            }
            else {
                cp.setImportFile(importFile);
            }
        }
    }

    private EuropeanaCollectionX createNewCollection(ImportFileX importFile) {
        EuropeanaCollectionX collection;
        collection = new EuropeanaCollectionX();
        collection.setName(importFile.deriveCollectionName());
        collection.setFileName(importFile.getFileName());
        collection.setDescription(importFile.getFileName());
        collection.setCollectionLastModified(new Date());
        collection.setFileUserName(world.user().getUserName());
        collection.setFileState(importFile.getState());
        collection.setCollectionState(CollectionStateX.EMPTY);
        return collection;
    }

    private Widget createCollectionSuggestBox() {
        final SuggestBox box = new SuggestBox(collectionList.getSuggestOracle());
        box.addSelectionHandler (new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection (SelectionEvent<SuggestOracle.Suggestion> event) {
                CollectionList.Suggestion suggestion = (CollectionList.Suggestion) event.getSelectedItem();
                addCollectionPanel(suggestion.getCollection());
                box.setText("");
            }
        });
        HorizontalPanel horiz = new HorizontalPanel();
        horiz.setWidth("100%");
        horiz.setSpacing(10);
        horiz.add(new HTML(world.messages().collectionChoose()));
        horiz.add(box);
        return horiz;
    }

    private CollectionPanel addCollectionPanel(EuropeanaCollectionX collection) {
        final CollectionPanel collectionPanel = new CollectionPanel(world, collection, collectionClose);
        collectionPanels.put(collection.getName(), collectionPanel);
        collectionList.setAvoidance(collection.getName(), true);
        collectionList.updateCollection(collection);
        mainPanel.add(collectionPanel.getWidget());
        collectionPanel.addCollectionUpdateListener(new CollectionHolder.CollectionUpdateListener() {
            public void collectionUpdated(EuropeanaCollectionX collection) {
                collectionList.updateCollection(collection);
            }
        });
        if (collection.getFileState() != ImportFileX.State.NONEXISTENT) {
            world.service().checkImportFileStatus(collection.getFileName(), true, new Reply<ImportFileX>() {
                public void onSuccess(ImportFileX result) {
                    collectionPanel.setImportFile(result);
                }
            });
        }
        return collectionPanel;
    }

    private class CollectionClose implements CollectionPanel.CloseNotifier {
        public void close(EuropeanaCollectionX collection) {
            CollectionPanel collectionPanel = collectionPanels.get(collection.getName());
            collectionPanel.getWidget().removeFromParent();
            collectionPanels.remove(collection.getName());
            collectionList.setAvoidance(collection.getName(), false);
        }
    }

    private class QueuePoller extends Timer {
        public void run() {
            world.service().fetchQueueEntries(new Reply<List<QueueEntryX>>() {
                public void onSuccess(List<QueueEntryX> result) {
                    Set<String> collectionsTouched = new HashSet<String>();
                    for (QueueEntryX entry : result) {
                        CollectionPanel collectionPanel = collectionPanels.get(entry.getCollection().getName());
                        if (collectionPanel == null) {
                            collectionPanel = addCollectionPanel(entry.getCollection());
                        }
                        collectionPanel.setQueueEntry(entry);
                        collectionsTouched.add(entry.getCollection().getName());
                    }
                    for (Map.Entry<String, CollectionPanel> entry : collectionPanels.entrySet()) {
                        if (!collectionsTouched.contains(entry.getKey())) {
                            final CollectionPanel collectionPanel = entry.getValue();
                            world.service().fetchCollection(entry.getKey(), null, false, new Reply<EuropeanaCollectionX>() {
                                public void onSuccess(EuropeanaCollectionX result) {
                                    if (result != null) {
                                        collectionPanel.setCollection(result);
                                    }
                                }
                            });
                        }
                    }
                    queuePoller.schedule(STATUS_CHECK_DELAY);
                }
            });
        }
    }


}
