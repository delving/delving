package eu.europeana.dashboard.client.collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.ImportFileX;
import eu.europeana.dashboard.client.dto.QueueEntryX;

/**
 * Hold an import file and present the appropriate widget for doing things with it.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionPanel extends DashboardWidget {
    private CollectionHolder holder;
    private HTML collectionName, collectionRecords;
    private VerticalPanel mainPanel = new VerticalPanel();
    private ImportFileStatusWidget importFileStatusWidget;
    private CollectionStatusWidget collectionStatusWidget;
    private CloseNotifier closeNotifier;

    public interface CloseNotifier {
        void close(EuropeanaCollectionX collection);
    }

    public CollectionPanel(World world, EuropeanaCollectionX collection, CloseNotifier closeNotifier) {
        super(world);
        this.holder = new CollectionHolder(world.service(), null, collection);
        this.importFileStatusWidget = new ImportFileStatusWidget(world, holder);
        this.collectionStatusWidget = new CollectionStatusWidget(world, holder);
        this.collectionName = new HTML(collection.getName());
        this.collectionName.setStyleName("collectionName");
        this.collectionRecords = new HTML(world.messages().collectionRecords(collection.getTotalRecords()));
        this.closeNotifier = closeNotifier;
        holder.addListener(new CollectionHolder.CollectionUpdateListener() {
            public void collectionUpdated(EuropeanaCollectionX collection) {
                refreshCollectionRecords();
            }
        });
    }

    public Widget createWidget() {
        HTML close = new HTML(world.messages().close());
        close.setStyleName("collectionClose");
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                closeNotifier.close(holder.getCollection());
            }
        });
        Button recount = new Button(world.messages().recount());
        recount.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                world.service().updateCollectionCounters(holder.getCollection(), new Reply<EuropeanaCollectionX>() {
                    public void onSuccess(EuropeanaCollectionX result) {
                        setCollection(result);
                    }
                });
            }
        });
        HorizontalPanel namePanel = new HorizontalPanel();
        namePanel.setSpacing(6);
        namePanel.add(collectionName);
        namePanel.add(collectionRecords);
        namePanel.add(recount);
        DockPanel root = new DockPanel();
        root.setWidth("100%");
        mainPanel.setSpacing(5);
        mainPanel.add(namePanel);
        mainPanel.add(importFileStatusWidget.getWidget());
        mainPanel.add(collectionStatusWidget.getWidget());
        root.add(mainPanel, DockPanel.CENTER);
        root.add(close, DockPanel.EAST);
        root.setCellHorizontalAlignment(close, DockPanel.ALIGN_RIGHT);
        DecoratorPanel decoratorPanel = new DecoratorPanel();
        decoratorPanel.setWidth("100%");
        decoratorPanel.setWidget(root);
        return decoratorPanel;
    }

    public void addCollectionUpdateListener(CollectionHolder.CollectionUpdateListener listener) {
        holder.addListener(listener);
    }

    public void setImportFileName(String fileName) {
        holder.setImportFileName(fileName);
        importFileStatusWidget.refreshPanel();
    }

    public void setImportFile(ImportFileX importFile) {
        if (importFile == null) {
            holder.clearImportFile();
        }
        else {
            holder.setImportFile(importFile);
        }
    }

    public void waitForFile() {
        importFileStatusWidget.waitForFile();
    }

    public void setQueueEntry(QueueEntryX entry) {
        collectionStatusWidget.setRecordsProcessed(entry.getRecordsProcessed());
        collectionStatusWidget.setTotalRecords(entry.getTotalRecords());
        holder.setCollection(entry.getCollection());
    }

    public void setCollection(EuropeanaCollectionX collection) {
        holder.setCollection(collection);
    }

    private void refreshCollectionRecords() {
        collectionRecords.setText(world.messages().collectionRecords(holder.getCollection().getTotalRecords()));
    }
}