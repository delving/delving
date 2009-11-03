package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 21, 2009: 6:39:02 PM
 */
public class GodWidget extends DashboardWidget {

    public GodWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(6);
        panel.setTitle("god panel");
        HTML disableAll = new HTML(world.messages().disableAll());
        disableAll.setStyleName("actionLink");
        disableAll.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                world.service().disableAllCollections(new Reply() {
                    public void onSuccess(Object o) {
                        System.out.println("disabled all collections");
                    }
                });
            }
        });
        panel.add(disableAll);
        HTML enableAll = new HTML(world.messages().enableAll());
        enableAll.setStyleName("actionLink");
        enableAll.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                world.service().enableAllCollections(new Reply() {
                    public void onSuccess(Object o) {
                        System.out.println("enabled all collections");
                    }
                });
            }
        });
        panel.add(enableAll);
        return panel;
    }
}
