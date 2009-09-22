package eu.europeana.dashboard.client.widgets;

import com.google.gwt.user.client.ui.DecoratedTabPanel;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.RoleX;
import eu.europeana.dashboard.client.dto.UserX;

/**
 * A tab panel where tabs appear conditionally based on the user's role
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class RoleBasedTabPanel extends DecoratedTabPanel {

    private UserX user;

    public RoleBasedTabPanel(UserX user) {
        this.user = user;
        setWidth("1000px");
        setHeight("100%");
        setAnimationEnabled(true);
    }

    public void addTab(DashboardWidget widget, String tabName, RoleX... roles) {
        boolean permitted = false;
        for (RoleX role : roles) {
            if (role == user.getRole()) {
                permitted = true;
            }
        }
        if (permitted) {
            add(widget.getWidget(), tabName);
        }
    }


}
