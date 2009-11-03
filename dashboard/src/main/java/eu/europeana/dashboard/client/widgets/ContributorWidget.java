package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.ContributorX;
import eu.europeana.dashboard.client.dto.CountryX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allow for editing of proposed search terms
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ContributorWidget extends DashboardWidget {
    private Tree tree = new Tree();

    public ContributorWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        world.service().fetchCountries(new Reply<List<CountryX>>() {
            public void onSuccess(final List<CountryX> countryList) {
                final Map<String, TreeItem> items = new HashMap<String, TreeItem>();
                for (CountryX country : countryList) {
                    items.put(country.getCode(), tree.addItem(new CountryLabel(country)));
                }
                world.service().fetchContributors(new Reply<List<ContributorX>>() {
                    public void onSuccess(List<ContributorX> contributorList) {
                        for (ContributorX contributor : contributorList) {
                            TreeItem countryItem = items.get(contributor.getCountry().getCode());
                            ContributorEditor editor = new ContributorEditor(countryItem, contributor);
                            editor.setPanelTreeItem(countryItem.addItem(editor));
                        }
                        for (CountryX country : countryList) {
                            TreeItem countryItem = items.get(country.getCode());
                            ContributorEditor editor = new ContributorEditor(countryItem, new ContributorX());
                            editor.setPanelTreeItem(countryItem.addItem(editor));
                        }
                    }
                });

            }
        });
        return tree;
    }

    private class CountryLabel extends HTML {
        private CountryX country;

        private CountryLabel(CountryX country) {
            super(country.getName());
            this.country = country;
        }

        public CountryX getCountry() {
            return country;
        }
    }

    private class ContributorEditor extends VerticalPanel {
        private ContributorX contributor;
        private TreeItem countryTreeItem, panelTreeItem;
        private HorizontalPanel item = new HorizontalPanel();
        private FlexTable fields = new FlexTable();
        private TextBox providerIdBox = new TextBox();
        private TextBox originalNameBox = new TextBox();
        private TextBox englishNameBox = new TextBox();
        private TextBox acronymBox = new TextBox();
        private TextBox numberOfPartnersBox = new TextBox();
        private TextBox urlBox = new TextBox();

        private ContributorEditor(TreeItem countryTreeItem, final ContributorX contributor) {
            this.contributor = contributor;
            this.countryTreeItem = countryTreeItem;
            if (contributor.getId() != null) {
                buildItemHtml();
            }
            else {
                buildMoreHtml();
            }
            buildFieldsPanel();
            this.add(item);
        }

        public void setPanelTreeItem(TreeItem panelTreeItem) {
            this.panelTreeItem = panelTreeItem;
        }

        private void buildItemHtml() {
            HTML itemHtml = new HTML("<a href=\"" + contributor.getUrl() + "\" target=\"_blank\">" + contributor.getOriginalName() + "</a>&nbsp;&nbsp;&nbsp;");
            HTML remove = new HTML("x");
            remove.setStyleName("actionLink");
            remove.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    world.service().removeContributor(contributor.getId(), new Reply<Boolean>() {
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

        private void buildFieldsPanel() {
            final Button submitButton = new Button(world.messages().submit());
            submitButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    saveContributor();
                }
            });
            final Button revertButton = new Button(world.messages().revert());
            revertButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                    revertToItem();
                }
            });
            FlexTable.FlexCellFormatter formatter = fields.getFlexCellFormatter();
            fields.setHTML(0, 0, world.messages().addContributorTitle(this.countryTreeItem.getText()));
            formatter.setStyleName(0, 0, "formTitle");
            formatter.setColSpan(0, 0, 2);
            fields.setHTML(1, 0, world.messages().providerIdPrompt());
            fields.setHTML(2, 0, world.messages().originalNamePrompt());
            fields.setHTML(3, 0, world.messages().englishNamePrompt());
            fields.setHTML(4, 0, world.messages().acroynmPrompt());
            fields.setHTML(5, 0, world.messages().numberOfPartnersPrompt());
            fields.setHTML(6, 0, world.messages().urlPrompt());
            fields.setWidget(1, 1, providerIdBox);
            fields.setWidget(2, 1, originalNameBox);
            fields.setWidget(3, 1, englishNameBox);
            fields.setWidget(4, 1, acronymBox);
            fields.setWidget(5, 1, numberOfPartnersBox);
            fields.setWidget(6, 1, urlBox);
            providerIdBox.setWidth("300px");
            originalNameBox.setWidth("300px");
            englishNameBox.setWidth("300px");
            acronymBox.setWidth("300px");
            urlBox.setWidth("600px");
            HorizontalPanel bp = new HorizontalPanel();
            bp.setSpacing(3);
            bp.add(revertButton);
            bp.add(submitButton);
            fields.setWidget(7, 0, bp);
            formatter.setColSpan(7, 0, 2);
            for (int walk = 1; walk <= 7; walk++) {
                formatter.setHorizontalAlignment(walk, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            }
        }

        private void saveContributor() {
            CountryLabel countryLabel = (CountryLabel) countryTreeItem.getWidget();
            final boolean fromScratch = contributor.getId() == null;
            contributor.setCountry(countryLabel.getCountry());
            contributor.setProviderId(providerIdBox.getText());
            contributor.setOriginalName(originalNameBox.getText());
            contributor.setEnglishName(englishNameBox.getText());
            contributor.setAcronym(acronymBox.getText());
            contributor.setNumberOfPartners(numberOfPartnersBox.getText());
            contributor.setUrl(urlBox.getText());
            world.service().saveContributor(contributor, new Reply<ContributorX>() {
                public void onSuccess(ContributorX freshContributor) {
                    contributor = freshContributor;
                    item.clear();
                    buildItemHtml();
                    revertToItem();
                    if (fromScratch) {
                        ContributorEditor editor = new ContributorEditor(countryTreeItem, new ContributorX());
                        editor.setPanelTreeItem(countryTreeItem.addItem(editor));
                    }
                }
            });
        }

        private void prepareForInput() {
            this.clear();
            DecoratorPanel surround = new DecoratorPanel();
            surround.setWidget(fields);
            if (contributor.getId() == null) {
                providerIdBox.setText("");
                originalNameBox.setText("");
                englishNameBox.setText("");
                acronymBox.setText("");
                numberOfPartnersBox.setText("");
                urlBox.setText("");
            }
            else {
                providerIdBox.setText(contributor.getProviderId());
                originalNameBox.setText(contributor.getOriginalName());
                englishNameBox.setText(contributor.getEnglishName());
                acronymBox.setText(contributor.getAcronym());
                numberOfPartnersBox.setText(contributor.getNumberOfPartners());
                urlBox.setText(contributor.getUrl());
            }
            this.add(surround);
        }

        private void revertToItem() {
            this.clear();
            this.add(item);
        }
    }
}