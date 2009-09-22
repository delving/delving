package eu.europeana.dashboard.client;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.dto.UserX;

/**
 * An abstract class to be superclass for all widgets
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public abstract class DashboardWidget {
    private Widget widget;
    protected World world;

    public interface World {
        UserX user();
        DashboardMessages messages();
        DashboardServiceAsync service();
    }

    public DashboardWidget(World world) {
        this.world = world;
    }

    public Widget getWidget() {
        if (widget == null) {
            VerticalPanel verticalPanel = new VerticalPanel();
            verticalPanel.setWidth("100%");
            verticalPanel.add(createWidget());
            widget = verticalPanel;
        }
        return widget;
    }

    public World getWorld() {
        return world;
    }

    protected abstract Widget createWidget();
}