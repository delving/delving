package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.SavedItemX;
import eu.europeana.dashboard.client.dto.UserX;

import java.util.Iterator;
import java.util.List;

/**
 * This is a component that lets you choose from anybody's saved items
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SavedItemChooser extends DashboardWidget {

    private Owner owner;
    private List<SavedItemX> items;
    private ListBox listBox;
    private Button selectButton;

    public SavedItemChooser(World world, Owner owner) {
        super(world);
        this.owner = owner;
    }

    public interface Owner {
        boolean avoidItem(SavedItemX savedItem);
        void selectItem(SavedItemX savedItem);
    }

    private void setItems(List<SavedItemX> items) {
        if ((this.items = items) == null) {
            listBox.clear();
            selectButton.setEnabled(false);
        }
        else {
            listBox.clear();
            listBox.addItem(world.messages().selectSavedItem());
            Iterator<SavedItemX> walk = items.iterator();
            while (walk.hasNext()) {
                SavedItemX item = walk.next();
                if (owner.avoidItem(item)) {
                    walk.remove();
                }
                else {
                    listBox.addItem(item.getTitle(), item.getUri());
                }
            }
            selectButton.setEnabled(false);
        }
    }

    @Override
    protected Widget createWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        listBox = new ListBox();
        listBox.setWidth("300px");
        selectButton = new Button(world.messages().select());
        selectButton.setEnabled(false);
        panel.setSpacing(6);
        UserChooser userChooser = new UserChooser(world);
        userChooser.setListener(new UserChooser.Listener() {
            @Override
            public void userSelected(UserX user) {
                world.service().fetchSavedItems(user.getId(), new Reply<List<SavedItemX>>() {
                    @Override
                    public void onSuccess(List<SavedItemX> result) {
                        setItems(result);
                    }
                });
            }
        });
        panel.add(userChooser.getWidget());
        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent sender) {
                int select = listBox.getSelectedIndex();
                selectButton.setEnabled(select > 0);
            }
        });
        panel.add(listBox);
        selectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent sender) {
                int index = listBox.getSelectedIndex()-1;
                owner.selectItem(items.get(index));
                listBox.removeItem(index);
                items.remove(index);
                selectButton.setEnabled(false);
                listBox.setSelectedIndex(0);
            }
        });
        panel.add(selectButton);
        return panel;
    }
}
