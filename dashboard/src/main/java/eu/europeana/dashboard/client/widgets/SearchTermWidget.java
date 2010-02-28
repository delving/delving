package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.LanguageX;
import eu.europeana.dashboard.client.dto.SavedSearchX;

import java.util.List;

/**
 * Allow for editing of proposed search terms
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SearchTermWidget extends DashboardWidget {

    private Tree tree = new Tree();

    public SearchTermWidget(World world) {
        super(world);
    }

    @Override
    protected Widget createWidget() {
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> item) {
            }

            public void onTreeItemStateChanged(final TreeItem item) {
                if (item.getState()) {
                    Widget widget = item.getWidget();
                    if (widget instanceof LanguageLabel) {
                        final LanguageX language = ((LanguageLabel) widget).getLanguage();
                        if (item.getChildCount() == 1) {
                            world.service().fetchSearchTerms(language.getCode(), new Reply<List<String>>() {
                                @Override
                                public void onSuccess(List<String> result) {
                                    TreeItem moreItem = item.getChild(0);
                                    moreItem.remove();
                                    for (String term : result) {
                                        TermPanel termPanel = new TermPanel(language, term);
                                        termPanel.setTreeItem(item.addItem(termPanel));
                                    }
                                    item.addItem(moreItem);
                                }
                            });
                        }
                    }
                }
            }
        });
        world.service().fetchLanguages(new Reply<List<LanguageX>>() {
            @Override
            public void onSuccess(List<LanguageX> result) {
                for (LanguageX language : result) {
                    TreeItem languageTreeItem = tree.addItem(new LanguageLabel(language));
                    AddSearchTermPanel add = new AddSearchTermPanel(language, languageTreeItem);
                    add.setPanelTreeItem(languageTreeItem.addItem(add));
                }
            }
        });
        return tree;
    }

    private class LanguageLabel extends HTML {
        private LanguageX language;

        private LanguageLabel(LanguageX language) {
            super(language.getName());
            this.language = language;
        }

        public LanguageX getLanguage() {
            return language;
        }
    }

    private class TermPanel extends HorizontalPanel {
        private String term;
        private TreeItem treeItem;

        public TermPanel(final LanguageX language, String termString) {
            this.term = termString;
            HTML termHtml = new HTML(term);
            this.add(termHtml);
            this.add(new HTML("&nbsp;&nbsp;&nbsp;"));
            HTML remove = new HTML("x");
            remove.setStyleName("actionLink");
            remove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)  {
                    world.service().removeSearchTerm(language.getCode(), term, new Reply<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            treeItem.remove();
                        }
                    });
                }
            });
            this.add(remove);
        }

        public void setTreeItem(TreeItem treeItem) {
            this.treeItem = treeItem;
        }
    }

    private class AddSearchTermPanel extends VerticalPanel implements SavedSearchChooser.Owner {
        private LanguageX language;
        private TreeItem languageTreeItem, panelTreeItem;
        private SavedSearchChooser savedSearchChooser = new SavedSearchChooser(world, this);
        private HTML option, prompt;
        private HorizontalPanel textBoxPanel = new HorizontalPanel();
        private TextBox queryStringBox = new TextBox();
        private Button submitButton = new Button(world.messages().submit());

        private AddSearchTermPanel(final LanguageX langauge, TreeItem item) {
            this.language = langauge;
            this.languageTreeItem = item;
            option = new HTML(world.messages().more());
            prompt = new HTML(world.messages().addSearchTermFor(languageTreeItem.getText()) + "  ");
            submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    final String queryString = queryStringBox.getText();
                    if (queryString.length() > 0) {
                        addSerchTerm(language, queryString);
                    }
                }
            });
            textBoxPanel.setSpacing(6);
            textBoxPanel.add(new HTML(world.messages().typeSearchTerm()));
            textBoxPanel.add(queryStringBox);
            textBoxPanel.add(submitButton);
            option.setStyleName("actionLink");
            option.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    prepareForInput();
                }
            });
            this.add(option);
        }

        public void setPanelTreeItem(TreeItem panelTreeItem) {
            this.panelTreeItem = panelTreeItem;
        }

        private void prepareForInput() {
            this.clear();
            DecoratorPanel surround = new DecoratorPanel();
            VerticalPanel vp = new VerticalPanel();
            vp.setSpacing(4);
            surround.setWidget(vp);
            queryStringBox.setText("");
            vp.add(prompt);
            vp.add(savedSearchChooser.getWidget());
            vp.add(textBoxPanel);
            this.add(surround);
        }

        private void revertToLink() {
            this.clear();
            this.add(option);
        }

        @Override
        public boolean avoidSearch(SavedSearchX savedSearch) {
            return !savedSearch.getLanguage().equals(language);
        }

        @Override
        public void selectSearch(final SavedSearchX savedSearch) {
            addSerchTerm(savedSearch.getLanguage(), savedSearch.getQueryString());
        }

        public void addSerchTerm(final LanguageX language, final String queryString) {
            world.service().addSearchTerm(language.getCode(), queryString, new Reply<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    languageTreeItem.removeItem(panelTreeItem);
                    revertToLink();
                    TermPanel termPanel = new TermPanel(language, queryString);
                    termPanel.setTreeItem(languageTreeItem.addItem(termPanel));
                    languageTreeItem.addItem(panelTreeItem);
                }
            });
        }
    }
}
