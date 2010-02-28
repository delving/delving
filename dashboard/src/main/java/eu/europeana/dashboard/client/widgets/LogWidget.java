package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.DashboardLogX;

import java.util.Iterator;
import java.util.List;

/**
 * Display audit logs
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */
public class LogWidget extends DashboardWidget {

    private static final int PAGE_SIZE = 30;
    private Grid grid = new Grid(PAGE_SIZE, 1);
    private Long topId, bottomId;

    public LogWidget(World world) {
        super(world);
    }

    @Override
    protected Widget createWidget() {
        grid.setBorderWidth(1);
        DecoratorPanel dp = new DecoratorPanel();
        dp.setWidget(createLogPanel());
        refresh();
        return dp;
    }

    private void refresh() {
        if (topId != null) {
            world.service().fetchLogEntriesFrom(topId, PAGE_SIZE, new Reply<List<DashboardLogX>>() {
                @Override
                public void onSuccess(List<DashboardLogX> result) {
                    populateGrid(result);
                }
            });
        }
        else {
            if (bottomId == null) {
                bottomId = Long.MAX_VALUE;
            }
            world.service().fetchLogEntriesTo(bottomId, PAGE_SIZE, new Reply<List<DashboardLogX>>() {
                @Override
                public void onSuccess(List<DashboardLogX> result) {
                    populateGrid(result);
                }
            });
        }
    }

    private void populateGrid(List<DashboardLogX> result) {
        if (result.isEmpty()) {
            for (int walk=0; walk<PAGE_SIZE; walk++) {
                grid.setHTML(walk, 0, "---");
            }
        }
        else {
            topId = result.get(0).getId();
            bottomId = result.get(result.size()-1).getId();
            Iterator<DashboardLogX> iterator = result.iterator();
            for (int walk=0; walk<PAGE_SIZE; walk++) {
                String line;
                if (iterator.hasNext()) {
                    DashboardLogX entry = iterator.next();
                    line = world.messages().logEntry(entry.getTime(), entry.getWho(), entry.getWhat());
                }
                else {
                    line = "---";
                }
                grid.setHTML(walk, 0, line);
            }
        }
    }

    private Widget createLogPanel() {
        grid.setWidth("900px");
        final Button up = new Button(world.messages().olderEntries());
        up.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent sender) {
                bottomId = topId;
                topId = null;
                refresh();
            }
        });
        final Button down = new Button(world.messages().newerEntries());
        down.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent sender) {
                topId = bottomId;
                bottomId = null;
                refresh();
            }
        });
        VerticalPanel p = new VerticalPanel();
        p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        p.setSpacing(6);
        p.add(up);
        p.add(grid);
        p.add(down);
        return p;
    }
}
