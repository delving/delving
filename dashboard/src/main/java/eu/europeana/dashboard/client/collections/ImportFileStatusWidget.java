package eu.europeana.dashboard.client.collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFile;

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
            public void collectionUpdated(EuropeanaCollectionX collection) {
                refreshPanel();
            }
        });
    }

    public Widget createWidget() {
        return panel;
    }

    public void refreshPanel() {
        panel.clear();
        if (holder.getImportFile() == null) {
            panel.add(new HTML(world.messages().noImportFilePresent()));
        }
        else {
            final ImportFile importFile = holder.getImportFile();
            panel.add(new HTML(world.messages().theFileIs(importFile.getFileName())));
            switch (importFile.getState()) {
                case NONEXISTENT:
                    panel.add(new HTML(world.messages().noImportFilePresent()));
                    checkTransitionFromState(ImportFile.State.NONEXISTENT);
                    break;
                case UPLOADING:
                    panel.add(new HTML(world.messages().uploading()));
                    checkTransitionFromState(ImportFile.State.UPLOADING);
                    break;
                case UPLOADED:
                    panel.add(new HTML(world.messages().uploaded()));
                    Button commenceImport = new Button(world.messages().commenceImport());
                    commenceImport.addClickListener(new ClickListener() {
                        public void onClick(Widget sender) {
                            world.service().commenceImport(importFile, holder.getCollection().getId(), true, new Reply<ImportFile>() {
                                public void onSuccess(ImportFile result) {
                                    holder.setImportFile(result);
                                }
                            });
                        }
                    });
                    panel.add(commenceImport);
                    panel.add(new HTML(world.messages().or()));
                    break;
                case IMPORTING:
                    panel.add(new HTML(world.messages().importing()));
                    Button cancelImport = new Button(world.messages().abortImport());
                    cancelImport.addClickListener(new ClickListener() {
                        public void onClick(Widget sender) {
                            world.service().abortImport(importFile, true, new Reply<ImportFile>() {
                                public void onSuccess(ImportFile result) {
                                    holder.setImportFile(result);
                                }
                            });
                        }
                    });
                    panel.add(cancelImport);
                    checkTransitionFromState(ImportFile.State.IMPORTING);
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
        checkTransitionFromState(ImportFile.State.NONEXISTENT);
    }

    private void checkTransitionFromState(ImportFile.State currentState) {
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
        private ImportFile.State currentState;

        private StatusCheckTimer(ImportFile.State currentState) {
            this.currentState = currentState;
        }

        public boolean checks(ImportFile.State currentState) {
            return this.currentState == currentState;
        }

        public void run() {
            GWT.log("checking for " + holder.getCollection().getFileName(), null);
            world.service().checkImportFileStatus(holder.getCollection().getFileName(), true, new Reply<ImportFile>() {
                public void onSuccess(ImportFile result) {
                    if (result != null) {
                        holder.setImportFile(result);
                        if (result.getState() == currentState) {
                            GWT.log("waiting for " + holder.getCollection().getName() + " from " + currentState, null);
                            StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                        }
                        else {
                            statusCheckTimer = null;
                        }
                    }
                    else if (currentState == ImportFile.State.NONEXISTENT) {
                        GWT.log("waiting for " + holder.getCollection().getName() + " from " + currentState, null);
                        StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                    }
                    else {
                        GWT.log("no import file named " + holder.getCollection().getFileName(), null);
                        holder.clearImportFile();
                        statusCheckTimer = null;
                    }
                }
            });
        }
    }
}
