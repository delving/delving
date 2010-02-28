/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.CollectionHolder;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.collections.CollectionStatusWidget;
import eu.europeana.dashboard.client.dto.EuropeanaCollectionX;
import eu.europeana.dashboard.client.dto.QueueEntryX;

/**
 * A simplified way of importing collections for sandbox testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CollectionWidget extends DashboardWidget {
    private TextArea descriptionArea = new TextArea();
    private Button submit;
    private HTML counts = new HTML();
    private ImportFileWidget importFileWidget;
    private CollectionStatusWidget collectionStatus;
    private CollectionHolder holder;

    public CollectionWidget(World world, EuropeanaCollectionX collection, CollectionHolder.CollectionAddListener collectionAddListener) {
        super(world);
        this.holder = new CollectionHolder(world.service(), collectionAddListener, collection);
        this.counts = new HTML(world.messages().dataSetRecords(collection.getTotalRecords()));
        this.importFileWidget = new ImportFileWidget(world, holder);
        this.collectionStatus = new CollectionStatusWidget(world, holder);
    }

    public CollectionWidget(World world, CollectionHolder.CollectionAddListener collectionAddListener, String name) {
        this(world, new EuropeanaCollectionX(name), collectionAddListener);
    }

    public void setQueueEntry(QueueEntryX entry) {
        this.counts.setText(world.messages().dataSetRecords(entry.getCollection().getTotalRecords()));
        collectionStatus.setRecordsProcessed(entry.getRecordsProcessed());
        collectionStatus.setTotalRecords(entry.getTotalRecords());
        holder.setCollection(entry.getCollection());
    }

    public void setCollection(EuropeanaCollectionX collection) {
        if (collection != null) {
            holder.setCollection(collection);
            this.counts.setText(world.messages().dataSetRecords(collection.getTotalRecords()));
        }
    }

    @Override
    public Widget createWidget() {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(4);
        panel.setWidth("100%");
        panel.add(createBasicsPanel());
        if (holder.getCollection().getId() != null) {
            panel.add(createImportPanel());
            panel.add(createIndexCachePanel());
        }
        DecoratorPanel decoratorPanel = new DecoratorPanel();
        decoratorPanel.setWidth("100%");
        decoratorPanel.setWidget(panel);
        return decoratorPanel;
    }

    private boolean descriptionEdited() {
        String current = holder.getCollection().getDescription();
        if (current == null) {
            current = "";
        }
        String entered = descriptionArea.getText().trim();
        if (!entered.equals(current)) {
            descriptionArea.setStyleName("editedValue");
            return true;
        }
        else {
            descriptionArea.setStyleName(null);
            return false;
        }
    }

    private Widget createBasicsPanel() {
        FlexTable flex = new FlexTable();
        flex.setWidth("100%");
        flex.setCellSpacing(10);
        FlexTable.FlexCellFormatter format = flex.getFlexCellFormatter();
        // name
        format.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        Label nameLabel = new Label(world.messages().dataset()+" "+holder.getCollection().getName());
        nameLabel.setStyleName("collectionName");
        flex.setWidget(0, 0, nameLabel);
        flex.setWidget(0, 1, counts);
        // description
        format.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        flex.setHTML(1, 0, world.messages().collectionDescription());
        descriptionArea.setVisibleLines(3);
        descriptionArea.setWidth("100%");
        descriptionArea.setText(holder.getCollection().getDescription());
        descriptionArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                submit.setEnabled(descriptionEdited());
            }
        });
        flex.setWidget(1, 1, descriptionArea);
        // submit button
        submit = new Button(world.messages().submit());
        submit.setEnabled(false);
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)  {
                if (descriptionEdited()) {
                    holder.getCollection().setDescription(descriptionArea.getText().trim());
                }
                descriptionEdited(); // descriptions are the same again, so return to normal color
                submit.setEnabled(false);
                world.service().updateCollection(holder.getCollection(), new Reply<EuropeanaCollectionX>() {
                    @Override
                    public void onSuccess(EuropeanaCollectionX collectionX) {
                        holder.setCollection(collectionX);
                    }
                });
            }
        });
        flex.setWidget(2, 1, submit);
        format.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        format.setWidth(0, 0, "20%");
        format.setWidth(0, 1, "80%");
        return flex;
    }

    private Widget createImportPanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(6);
        panel.add(importFileWidget.getWidget());
        return createDisclosurePanel(panel, world.messages().importTitle());
    }

    private Widget createIndexCachePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.add(collectionStatus.getWidget());
        return createDisclosurePanel(panel, world.messages().indexTitle());
    }

    private Widget createDisclosurePanel(Widget widget, String title) {
        DisclosurePanel disclosurePanel = new DisclosurePanel(title);
        disclosurePanel.setAnimationEnabled(true);
        disclosurePanel.setContent(widget);
        return disclosurePanel;
    }
}