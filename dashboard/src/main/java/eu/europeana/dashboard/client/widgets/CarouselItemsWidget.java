package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.CarouselItemX;
import eu.europeana.dashboard.client.dto.SavedItemX;

import java.util.List;

/**
 * Allow dashboarders to manage a list of carousel items
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CarouselItemsWidget extends DashboardWidget {
    private static final int COLUMNS = 8;
    private String cacheUrl;
    private Grid grid;
    private VerticalPanel panel = new VerticalPanel();
    private DecoratedPopupPanel popup = new DecoratedPopupPanel();
    private List<CarouselItemX> carouselItems;

    public CarouselItemsWidget(World world) {
        super(world);
    }

    @Override
    protected Widget createWidget() {
        panel.setWidth("100%");
        panel.add(new SavedItemChooser(world, new SavedItemChooserOwner()).createWidget());
        world.service().fetchCacheUrl(new Reply<String>() {
            @Override
            public void onSuccess(String result) {
                cacheUrl = result;
                refreshGrid();
            }
        });
        return panel;
    }

    private void refreshGrid() {
        world.service().fetchCarouselItems(new Reply<List<CarouselItemX>>() {
            @Override
            public void onSuccess(List<CarouselItemX> items) {
                carouselItems = items;
                if (grid != null) {
                    panel.remove(grid);
                }
                int totalRows = items.size() / COLUMNS;
                if (items.size() % COLUMNS != 0) {
                    totalRows++;
                }
                grid = new Grid(totalRows, COLUMNS);
                grid.setCellPadding(10);
                int count = 0;
                for (CarouselItemX item : items) {
                    int row = count / COLUMNS;
                    int col = count % COLUMNS;
                    String imageSource = cacheUrl + "uri=" + URL.encode(item.getThumbnail()) + "&size=BRIEF_DOC&type=IMAGE";
                    HTML thumb = new HTML("<img src=\"" + imageSource + "\">");
                    thumb.addClickHandler(new PopupClickListener(item));
                    grid.setWidget(row, col, thumb);
                    count++;
                }
                panel.add(grid);
            }
        });
    }

    private class SavedItemChooserOwner implements SavedItemChooser.Owner {
        @Override
        public boolean avoidItem(SavedItemX savedItem) {
            if (carouselItems != null) {
                for (CarouselItemX item : carouselItems) {
                    if (item.getEuropeanaUri().equals(savedItem.getUri())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void selectItem(final SavedItemX savedItem) {
            world.service().createCarouselItem(savedItem, new Reply<CarouselItemX>() {
                @Override
                public void onSuccess(CarouselItemX result) {
                    if (result == null) {
                        Window.alert(world.messages().uriNotFound(savedItem.getUri()));
                    }
                    else {
                        refreshGrid();
                    }
                }
            });
        }
    }

    private class PopupClickListener implements ClickHandler {
        private static final int OFFSET = 30;
        private CarouselItemX item;

        private PopupClickListener(CarouselItemX item) {
            this.item = item;
        }

        @Override
        public void onClick(ClickEvent sender) {
            popup.setPopupPosition(((Widget)sender.getSource()).getAbsoluteLeft() + OFFSET, ((Widget)sender.getSource()).getAbsoluteTop() + 2 * OFFSET);
            Grid grid = new Grid(6, 2);
            grid.setHTML(0, 0, world.messages().title());
            grid.setHTML(1, 0, world.messages().creator());
            grid.setHTML(2, 0, world.messages().year());
            grid.setHTML(3, 0, world.messages().provider());
            grid.setHTML(4, 0, world.messages().language());
            grid.setHTML(5, 0, world.messages().type());
            grid.setHTML(0, 1, item.getTitle());
            grid.setHTML(1, 1, item.getCreator());
            grid.setHTML(2, 1, item.getYear());
            grid.setHTML(3, 1, item.getProvider());
            grid.setHTML(4, 1, item.getLanguage());
            grid.setHTML(5, 1, item.getType().toString());
            grid.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    HTMLTable.Cell cell= ((Grid)event.getSource()).getCellForEvent(event);
                    if (cell != null){
                        // todo ???
                    }
                    popup.hide();         // inside the if block ??
                }
            });
            VerticalPanel p = new VerticalPanel();
            p.setWidth("100%");
            p.add(grid);
            Button delete = new Button(world.messages().deleteThisItem());
            delete.addClickHandler(new ClickHandler() {
               @Override
               public void onClick(ClickEvent sender) {
                    world.service().removeCarouselItem(item, new Reply<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result) {
                                popup.hide();
                                refreshGrid();
                            }
                        }
                    });
                }
            });
            p.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            p.add(delete);
            popup.setWidget(p);
            popup.show();
        }

    }
}
