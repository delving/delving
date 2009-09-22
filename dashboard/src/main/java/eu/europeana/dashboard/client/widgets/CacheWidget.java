package eu.europeana.dashboard.client.widgets;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;

import java.util.List;

/**
 * A widget to handle collections
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CacheWidget extends DashboardWidget {

    private VerticalPanel logPanel = new VerticalPanel();
    private VerticalPanel orphanPanels = new VerticalPanel();
    private List<String> objectOrphans;

    public CacheWidget(World world) {
        super(world);
        world.service().getObjectOrphans(new Reply<List<String>>() {
            public void onSuccess(List<String> result) {
                objectOrphans = result;
                orphanPanels.clear();
                for (String uri : objectOrphans) {
                    orphanPanels.add(new OrphanPanel(uri));
                }
            }
        });
    }

    protected Widget createWidget() {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(10);
        DecoratorPanel logDecorator = new DecoratorPanel();
        logDecorator.setTitle("Cache Log?");
        logDecorator.setWidget(logPanel);
        panel.add(logDecorator);
        DecoratorPanel orphanDecorator = new DecoratorPanel();
        orphanDecorator.setTitle("Orphans?");
        orphanDecorator.setWidget(orphanPanels);
        panel.add(orphanDecorator);
        Button deleteAllOrphans = new Button(world.messages().cacheDeleteAllOrphans());
        deleteAllOrphans.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                world.service().deleteAllOrphans(new Reply<Void>() {
                    public void onSuccess(Void result) {
                        orphanPanels.clear();
                        objectOrphans = null;
                    }
                });
            }
        });
        panel.add(deleteAllOrphans);
        return panel;
    }

    private class OrphanPanel extends HorizontalPanel {
        private OrphanPanel(final String uri) {
            setSpacing(6);
            add(new HTML("<a href=\""+uri+"\" target=\"_orphanFrame\">"+uri+"</a>"));
            Button delete = new Button(world.messages().cacheDeleteThisOne());
            add(delete);
            delete.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    world.service().deleteObjectOrphan(uri, new Reply<Boolean>() {
                        public void onSuccess(Boolean result) {
                            OrphanPanel.this.removeFromParent();
                        }
                    });
                }
            });
        }
    }


}