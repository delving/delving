package eu.europeana.dashboard.client.collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.CacheStateX;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.sandbox.VerifyDialog;

/**
 * A panel that shows collection state and allows for changing it and aborting when it's underway
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CacheStatusWidget extends DashboardWidget {
    private HorizontalPanel panel = new HorizontalPanel();
    private CollectionHolder holder;
    private int recordsProcessed;
    private int totalRecords;

    public CacheStatusWidget(World world, CollectionHolder holder) {
        super(world);
        panel.setSpacing(6);
        this.holder = holder;
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
        String theyAre = world.messages().itsThumbnailsAre() + " ";
        switch (holder.getCollection().getCacheState()) {
            case EMPTY:
                panel.add(new HTML(theyAre + world.messages().notReadyToCache()));
                break;
            case UNCACHED:
                panel.add(new HTML(theyAre + world.messages().notCached()));
                final Button commence = new Button(world.messages().commenceCacheing());
                commence.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent sender) {
                        askSetState(CacheStateX.QUEUED, world.messages().areYouSureCollection(world.messages().cache()));
                    }
                });
                panel.add(commence);
                break;
            case QUEUED:
                panel.add(new HTML(theyAre + world.messages().queuedForCacheing()));
                panel.add(createAbortLink(CacheStateX.UNCACHED, world.messages().cacheing()));
                break;
            case CACHEING:
                panel.add(new HTML(theyAre + world.messages().cacheingProgress(recordsProcessed, totalRecords)));
                panel.add(createAbortLink(CacheStateX.UNCACHED, world.messages().cacheing()));
                break;
            case CACHED:
                panel.add(new HTML(theyAre + world.messages().cached()));
                final Button recache = new Button(world.messages().recache());
                recache.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent sender) {
                        askSetState(CacheStateX.QUEUED, world.messages().areYouSureCollection(world.messages().recache()));
                    }
                });
                panel.add(recache);
                break;
            default:
                throw new RuntimeException("Unknown state");
        }
    }

    private Widget createAbortLink(final CacheStateX abortedCacheState, final String process) {
        final Button abort = new Button(world.messages().abort());
        abort.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                askSetState(abortedCacheState, world.messages().areYouSureAbort(process));
            }
        });
        return abort;
    }

    private void askSetState(final CacheStateX toCacheState, String question) {
        VerifyDialog verifyDialog = new VerifyDialog(
                this,
                world.messages().collectionEnablementCaption(),
                question
        );
        verifyDialog.ask(
                new Runnable() {
                    public void run() {
                        holder.setCacheState(toCacheState);
                    }
                }
        );
    }
}