package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
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

public class ImportFileWidget extends DashboardWidget {
    private static final int STATUS_CHECK_DELAY = 2000;
    private HorizontalPanel panel = new HorizontalPanel();
    private CollectionHolder collectionHolder;
    private FileUploadWidget fileUploadWidget;
    private StatusCheckTimer statusCheckTimer;

    public ImportFileWidget(World world, CollectionHolder collectionHolder) {
        super(world);
        this.collectionHolder = collectionHolder;
        this.panel.setSpacing(15);
        fileUploadWidget = new FileUploadWidget(world);
        fileUploadWidget.setNotifier(new FileUploadWidget.Status() {
            public void uploadStarted(String fileName) {
                ImportFileWidget.this.collectionHolder.setImportFileName(fileName);
            }
            public void uploadEnded() {
                checkTransitionFromState(ImportFile.State.NONEXISTENT);
            }
        });
        if (collectionHolder.getCollection().getFileName() != null) {
            checkStatus();
        }
        refreshPanel();
    }

    public Widget createWidget() {
        return panel;
    }

    private void refreshPanel() {
        panel.clear();
        if (collectionHolder.getImportFile() == null) {
            panel.add(fileUploadWidget.getWidget());
        }
        else {
            String theFileIs = world.messages().theFileIs(collectionHolder.getImportFile().getFileName())+" ";
            switch (collectionHolder.getImportFile().getState()) {
                case NONEXISTENT:
                    panel.add(fileUploadWidget.getWidget());
                    break;
                case UPLOADING:
                    panel.add(new HTML(theFileIs+world.messages().uploading()));
                    checkTransitionFromState(ImportFile.State.UPLOADING);
                    break;
                case UPLOADED:
                    panel.add(new HTML(theFileIs+world.messages().uploaded()));
                    world.service().commenceValidate(collectionHolder.getImportFile(), collectionHolder.getCollection().getId(), new Reply<ImportFile>() {
                        public void onSuccess(ImportFile result) {
                            collectionHolder.setImportFile(result);
                            refreshPanel();
                        }
                    });
                    break;
                case VALIDATING:
                    panel.add(new HTML(theFileIs+world.messages().validating()));
                    checkTransitionFromState(ImportFile.State.VALIDATING);
                    break;
                case VALIDATED:
                    panel.add(new HTML(theFileIs+world.messages().validated()));
                    world.service().commenceImport(collectionHolder.getImportFile(), collectionHolder.getCollection().getId(), false, new Reply<ImportFile>() {
                        public void onSuccess(ImportFile result) {
                            collectionHolder.setImportFile(result);
                            refreshPanel();
                        }
                    });
                    break;
                case IMPORTING:
                    panel.add(new HTML(theFileIs+world.messages().importing()));
                    Button cancelImport = new Button(world.messages().abortImport());
                    cancelImport.addClickListener(new ClickListener() {
                        public void onClick(Widget sender) {
                            world.service().abortImport(collectionHolder.getImportFile(), false, new Reply<ImportFile>() {
                                public void onSuccess(ImportFile result) {
                                    collectionHolder.setImportFile(result);
                                    refreshPanel();
                                }
                            });
                        }
                    });
                    panel.add(cancelImport);
                    checkTransitionFromState(ImportFile.State.IMPORTING);
                    break;
                case IMPORTED:
                    EuropeanaCollectionX c = collectionHolder.getCollection();
                    if (c.getFileName() != null && c.getCollectionLastModified() != null && c.getFileUserName() != null) {
                        panel.add(new HTML(theFileIs+world.messages().importFileStatus(c.getCollectionLastModified(), c.getFileUserName())));
                    }
                    else {
                        panel.add(new HTML(theFileIs+world.messages().imported())); // maybe never used
                    }
                    addDeleteClickable();
                    break;
                case ERROR:
                    String error = collectionHolder.getCollection().getImportError();
                    if (error == null) {
                        world.service().fetchCollection(collectionHolder.getCollection().getName(), false, new Reply<EuropeanaCollectionX>() {
                            public void onSuccess(EuropeanaCollectionX result) {
                                collectionHolder.setCollection(result);
                                refreshPanel();
                            }
                        });
                        panel.add(new HTML(theFileIs+world.messages().haltedWithAnError()));
                    }
                    else {
                        String [] parts = error.split("\n");
                        error = "";
                        for (String part : parts) {
                            error += "<br>";
                            error += part;
                        }
                        panel.add(new HTML(
                                theFileIs+world.messages().haltedWithAnError()+
                                        "<div style=\"color: firebrick; font-family:monospace; font-size=small; \">"+error+"</div>"
                        ));
                    }
                    addDeleteClickable();
                    break;
                default:
                    throw new RuntimeException("Unknown state");
            }
        }
    }

    private void addDeleteClickable() {
        Button delete = new Button(world.messages().deleteFile());
        delete.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                world.service().deleteImportFile(collectionHolder.getImportFile(), false, new Reply<Boolean>() {
                    public void onSuccess(Boolean result) {
                        collectionHolder.clearImportFile();
                        refreshPanel();
                    }
                });
            }
        });
        panel.add(delete);
    }

    private void checkStatus() {
        world.service().checkImportFileStatus(collectionHolder.getCollection().getFileName(), false, new Reply<ImportFile>() {
            public void onSuccess(ImportFile importFile) {
                if (importFile != null) {
                    collectionHolder.setImportFile(importFile);
                }
                else {
                    collectionHolder.clearImportFile();
                }
                refreshPanel();
            }
        });
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
            GWT.log("checking for " + collectionHolder.getCollection().getFileName(), null);
            world.service().checkImportFileStatus(collectionHolder.getCollection().getFileName(), false, new Reply<ImportFile>() {
                public void onSuccess(ImportFile result) {
                    if (result != null) {
                        collectionHolder.setImportFile(result);
                        if (result.getState() == currentState) {
                            GWT.log("waiting for " + collectionHolder.getCollection().getName() + " from " + currentState, null);
                            StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                        }
                        else {
                            statusCheckTimer = null;
                        }
                    }
                    else if (currentState == ImportFile.State.NONEXISTENT){
                        GWT.log("waiting for " + collectionHolder.getCollection().getName() + " from " + currentState, null);
                        StatusCheckTimer.this.schedule(STATUS_CHECK_DELAY);
                    }
                    else {
                        GWT.log("no import file for " + collectionHolder.getCollection().getName(), null);
                        collectionHolder.clearImportFile();
                        statusCheckTimer = null;
                    }
                    refreshPanel();
                }
            });
        }
    }
}