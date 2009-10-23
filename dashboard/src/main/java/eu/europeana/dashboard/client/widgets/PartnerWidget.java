package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.PartnerX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allow for editing of proposed search terms
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class PartnerWidget extends DashboardWidget {
    private Tree tree = new Tree();

    public PartnerWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        world.service().fetchPartnerSectors(new Reply<List<String>>() {
            public void onSuccess(final List<String> sectorList) {
                final Map<String, TreeItem> items = new HashMap<String, TreeItem>();
                for (String sector : sectorList) {
                    items.put(sector, tree.addItem(sector));
                }
                world.service().fetchPartners(new Reply<List<PartnerX>>() {
                    public void onSuccess(List<PartnerX> partnerList) {
                        for (PartnerX partner : partnerList) {
                            TreeItem sectorItem = items.get(partner.getSector());
                            PartnerEditor editor = new PartnerEditor(sectorItem, partner);
                            editor.setPanelTreeItem(sectorItem.addItem(editor));
                        }
                        for (String sector : sectorList) {
                            TreeItem sectorItem = items.get(sector);
                            PartnerEditor editor = new PartnerEditor(sectorItem, new PartnerX());
                            editor.setPanelTreeItem(sectorItem.addItem(editor));
                        }
                    }
                });
            }
        });
        return tree;
    }

    private class PartnerEditor extends VerticalPanel {
        private PartnerX partner;
        private HorizontalPanel item = new HorizontalPanel();
        private TreeItem sectorTreeItem, panelTreeItem;
        private FlexTable fields = new FlexTable();
        private TextBox nameBox = new TextBox();
        private TextBox urlBox = new TextBox();

        private PartnerEditor(TreeItem sectorTreeItem, PartnerX partner) {
            this.sectorTreeItem = sectorTreeItem;
            this.partner = partner;
            if (partner.getId() != null) {
                buildItemHtml();
            }
            else {
                buildMoreHtml();
            }
            buildPartnerFieldsPanel();
            this.add(item);
        }

        private void buildItemHtml() {
            HTML itemHtml = new HTML("<a href=\"" + partner.getUrl() + "\" target=\"_blank\">" + partner.getName() + "</a>&nbsp;&nbsp;&nbsp;");
            HTML remove = new HTML("x");
            remove.setStyleName("actionLink");
            remove.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    world.service().removePartner(partner.getId(), new Reply<Boolean>() {
                        public void onSuccess(Boolean result) {
                            if (result) {
                                panelTreeItem.remove();
                            }
                        }
                    });
                }
            });
            HTML edit = new HTML("e");
            edit.setStyleName("actionLink");
            edit.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    prepareForInput();
                }
            });
            item.add(itemHtml);
            item.add(remove);
            item.add(new HTML("&nbsp;&nbsp;"));
            item.add(edit);
        }

        private void buildMoreHtml() {
            HTML itemHtml = new HTML(world.messages().more());
            itemHtml.setStyleName("actionLink");
            itemHtml.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    prepareForInput();
                }
            });
            item.add(itemHtml);
        }

        private void buildPartnerFieldsPanel() {
            final Button submitButton = new Button(world.messages().submit());
            submitButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    savePartner();
                }
            });
            final Button revertButton = new Button(world.messages().revert());
            revertButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    revertToItem();
                }
            });
            FlexTable.FlexCellFormatter formatter = fields.getFlexCellFormatter();
            fields.setHTML(0, 0, world.messages().addPartnerTitle(this.sectorTreeItem.getText()));
            formatter.setStyleName(0, 0, "formTitle");
            formatter.setColSpan(0, 0, 2);
            fields.setHTML(1, 0, world.messages().namePrompt());
            formatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            fields.setHTML(2, 0, world.messages().urlPrompt());
            formatter.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            fields.setWidget(1, 1, nameBox);
            nameBox.setWidth("400px");
            fields.setWidget(2, 1, urlBox);
            urlBox.setWidth("600px");
            HorizontalPanel bp = new HorizontalPanel();
            bp.setSpacing(3);
            bp.add(revertButton);
            bp.add(submitButton);
            fields.setWidget(3, 0, bp);
            formatter.setColSpan(3, 0, 2);
            formatter.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        }

        private void savePartner() {
            partner.setSector(sectorTreeItem.getText());
            final boolean fromScratch = partner.getId() == null;
            partner.setName(nameBox.getText());
            partner.setUrl(urlBox.getText());
            world.service().savePartner(partner, new Reply<PartnerX>() {
                public void onSuccess(PartnerX result) {
                    partner = result;
                    item.clear();
                    buildItemHtml();
                    revertToItem();
                    if (fromScratch) {
                        PartnerEditor editor = new PartnerEditor(sectorTreeItem, new PartnerX());
                        editor.setPanelTreeItem(sectorTreeItem.addItem(editor));
                    }
                }
            });
        }

        public void setPanelTreeItem(TreeItem panelTreeItem) {
            this.panelTreeItem = panelTreeItem;
        }

        private void prepareForInput() {
            this.clear();
            if (partner.getId() == null) {
                nameBox.setText("");
                urlBox.setText("");
            }
            else {
                nameBox.setText(partner.getName());
                urlBox.setText(partner.getUrl());
            }
            DecoratorPanel surround = new DecoratorPanel();
            surround.setWidget(fields);
            this.add(surround);
        }

        private void revertToItem() {
            this.clear();
            this.add(item);
        }
    }
}