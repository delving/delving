package eu.europeana.dashboard.client.collections;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.CollectionStateX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.sandbox.VerifyDialog;

/**
 * A panel that shows collection state and allows for changing it and aborting when it's underway
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionStatusWidget extends DashboardWidget {
    private HorizontalPanel panel = new HorizontalPanel();
    private CollectionHolder holder;
    private int recordsProcessed;
    private int totalRecords;

    public CollectionStatusWidget(World world, CollectionHolder holder) {
        super(world);
        this.holder = holder;
        panel.setSpacing(6);
        refreshPanel();
        holder.addListener(new CollectionHolder.CollectionUpdateListener() {
            public void collectionUpdated(EuropeanaCollectionX collection) {
                refreshPanel();
            }
        });
    }

    protected Widget createWidget() {
        return panel;
    }

    public void setRecordsProcessed(int recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    private void refreshPanel() {
        panel.clear();
        String setIs = world.messages().dataSetIs() + " ";
        switch (holder.getCollection().getCollectionState()) {
            case EMPTY:
                panel.add(new HTML(setIs + world.messages().notReadyToIndex()));
                break;
            case DISABLED:
                panel.add(new HTML(setIs + world.messages().disabled()));
                final Button enable = new Button(world.messages().commenceIndexing());
                enable.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {
                        askSetState(CollectionStateX.QUEUED, world.messages().areYouSureCollection(world.messages().indexAndEnable()));
                    }
                });
                panel.add(enable);
                break;
            case QUEUED:
                panel.add(new HTML(setIs + world.messages().queuedForIndexing()));
                panel.add(createAbortLink(CollectionStateX.DISABLED, world.messages().indexing()));
                break;
            case INDEXING:
                panel.add(new HTML(setIs + world.messages().indexingProgress(recordsProcessed, totalRecords)));
                panel.add(createAbortLink(CollectionStateX.DISABLED, world.messages().indexing()));
                break;
            case ENABLED:
                panel.add(new HTML(setIs + " " + world.messages().enabled()));
                final Button disable = new Button(world.messages().disable());
                disable.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {
                        askSetState(CollectionStateX.DISABLED, world.messages().areYouSureCollection(world.messages().disable()));
                    }
                });
                panel.add(disable);
                break;
            default:
                throw new RuntimeException("Unknown state");
        }
    }

    private Widget createAbortLink(final CollectionStateX abortedCollectionState, final String process) {
        final Button abort = new Button(world.messages().abort());
        abort.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                askSetState(abortedCollectionState, world.messages().areYouSureAbort(process));
            }
        });
        return abort;
    }

    private void askSetState(final CollectionStateX toCollectionState, String question) {
        VerifyDialog verifyDialog = new VerifyDialog(
                this,
                world.messages().collectionEnablementCaption(),
                question
        );
        verifyDialog.ask(
                new Runnable() {
                    public void run() {
                        holder.setCollectionState(toCollectionState);
                    }
                }
        );
    }
}
