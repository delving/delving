package eu.europeana.dashboard.client.collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;

/**
 * Hold an import file and present the appropriate widget for doing things with it.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ImportFileStatusWidget extends DashboardWidget {
    private static final int STATUS_CHECK_DELAY = 1000;
    private CollectionHolder holder;
    private StatusCheckTimer statusCheckTimer;
    private HorizontalPanel panel = new HorizontalPanel();

    public ImportFileStatusWidget(World world, CollectionHolder holder) {
        super(world);
        this.holder = holder;
        this.panel.setSpacing(6);
        refreshPanel();
        holder.addListener(new CollectionHolder.CollectionUpdateListener() {
            @Override
            public void collectionUpdated(EuropeanaCollectionX collection) {
                refreshPanel();
            }
        });
    }

    @Override
    public Widget createWidget() {
        return panel;
    }

    public void refreshPanel() {
        panel.clear();
        if (holder.getImportFile() == null) {
            panel.add(new HTML(world.messages().noImportFilePresent()));
        }
        else {
            final ImportFileX importFile = holder.getImportFile();
            panel.add(new HTML(world.messages().theFileIs(importFile.getFileName())));
            switch (importFile.getState()) {
                case NONEXISTENT:
                    panel.add(new HTML(world.messages().noImportFilePresent()));
                    checkTransitionFromState(ImportFileX.State.NONEXISTENT);
                    break;
                case UPLOADING:
                    panel.add(new HTML(world.messages().uploading()));
                    checkTransitionFromState(ImportFileX.State.UPLOADING);
                    break;
                case UPLOADED:
                    panel.add(new HTML(world.messages().uploaded()));
                    Button commenceImport = new Button(world.messages().commenceImport());
                    commenceImport.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent sender) {
                            world.service().commenceImport(importFile, holder.getCollection().getId(), true, new Reply<ImportFileX>() {
                                @Override
                                public void onSuccess(ImportFileX result) {
                                    holder.setImportFile(result);
                                }
                            });
                        }
                    });
                    panel.add(commenceImport);
                    break;
                case IMPORTING:
                    panel.add(new HTML(world.messages().importing()));
                    Button cancelImport = new Button(world.messages().abortImport());
                    cancelImport.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent sender) {
                            world.service().abortImport(importFile, true, new Reply<ImportFileX>() {
                                @Override
                                public void onSuccess(ImportFileX result) {
                                    holder.setImportFile(result);
                                }
                            });
                        }
                    });
                    panel.add(cancelImport);
                    checkTransitionFromState(ImportFileX.State.IMPORTING);
                    break;
                case IMPORTED:
                    panel.add(new HTML(world.messages().imported()));
                    break;
                case ERROR:
                    panel.add(new HTML(world.messages().haltedWithAnError()));
                    break;
                default:
                    throw new RuntimeException("Unknown state");
            }
        }
    }

    public void waitForFile() {
        checkTransitionFromState(ImportFileX.State.NONEXISTENT);
    }

    private void checkTransitionFromState(ImportFileX.State currentState) {
        if (statusCheckTimer != null) {
            if (!statusCheckTimer.checks(currentState)) {
                statusCheckTimer.cancel();
                statusCheckTimer = new StatusCheckTimer(currentState);
                statusCheckTimer.schedule(STATUS_CHECK_DELAY);
            }
        }
        else {
            statusCheckTimer = new StatusCheckTimer(currentState);
            statusCheckTimer.schedule(STATUS_CHECK_DELAY);
        }
    }

    private class StatusCheckTimer extends Timer {
        private ImportFileX.State currentState;

        private StatusCheckTimer(ImportFileX.State currentState) {
            this.currentState = currentState;
        }

        public boolean checks(ImportFileX.State currentState) {
            return this.currentState == currentState;
        }

        @Override
        public void run() {
            world.service().checkImportFileStatus(holder.getCollection().getFileName(), true, new Reply<ImportFileX>() {
                @Override
                public void onSuccess(ImportFileX result) {
                    if (result != null) {
                        holder.setImportFile(result);
                        if (result.getState() == currentState) {
                            StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                        }
                        else {
                            statusCheckTimer = null;
                        }
                    }
                    else if (currentState == ImportFileX.State.NONEXISTENT) {
                        StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                    }
                    else {
                        holder.clearImportFile();
                        statusCheckTimer = null;
                    }
                }
            });
        }
    }
}
