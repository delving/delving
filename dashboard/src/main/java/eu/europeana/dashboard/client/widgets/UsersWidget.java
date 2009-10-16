package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.RoleX;
import eu.europeana.dashboard.client.dto.UserX;

/**
 * A widget for editing users
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UsersWidget extends DashboardWidget {
    private CheckBox enabledBox;
    private Button deleteButton;
    private ListBox roleBox;
    private UserChooser userChooser;
    private TextBox languagesBox = new TextBox();
    private TextBox projectIdBox = new TextBox();
    private TextBox providerIdBox = new TextBox();
    private VerifyDialog verifyDialog;

    public UsersWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        enabledBox = new CheckBox(world.messages().userEnabled());
        userChooser = new UserChooser(world);
        roleBox = new ListBox();
        for (RoleX role : RoleX.values()) {
            if (world.user().getRole() != RoleX.ROLE_GOD && role == RoleX.ROLE_GOD) {
                continue; // cannot switch anybody to god role unles you are god
            }
            roleBox.addItem(role.toString());
        }
        roleBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent sender) {
                world.service().setUserRole(userChooser.getSelectedUser().getId(), RoleX.values()[roleBox.getSelectedIndex()], new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // not sure what to do
                    }
                });
            }
        });
        deleteButton = new Button(world.messages().deleteThisUser());
        verifyDialog = new VerifyDialog(world.messages());
        userChooser.setListener(new UserChooser.Listener() {
            public void userSelected(UserX user) {
                enabledBox.setValue(user.isEnabled());
                roleBox.setSelectedIndex(user.getRole().ordinal());
                languagesBox.setText(user.getLanguages());
                projectIdBox.setText(user.getProjectId());
                providerIdBox.setText(user.getProviderId());
            }
        });
        enabledBox.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                if (userChooser.getSelectedUser() != null) {
                    world.service().setUserEnabled(userChooser.getSelectedUser().getId(), enabledBox.getValue(), new Reply<Void>() {
                        public void onSuccess(Void result) {
                            // not sure what to do
                        }
                    });
                }
            }
        });
        deleteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                verifyDialog.ask(deleteButton, world.messages().deleteCaption(), world.messages().deleteThisUserQuestion(), new Runnable() {
                    public void run() {
                        world.service().removeUser(userChooser.getSelectedUser().getId(), new Reply<Void>() {
                            public void onSuccess(Void result) {
                                userChooser.clear();
                            }
                        });
                    }
                });
            }
        });
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(10);
        vp.add(roleBox);
        vp.add(enabledBox);
        vp.add(createLanguagesPanel());
        vp.add(createProjectIdPanel());
        vp.add(createProviderIdPanel());
        vp.add(deleteButton);
        DecoratorPanel fields = new DecoratorPanel();
        fields.setWidget(vp);
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(userChooser.getWidget());
        hp.add(fields);
        return hp;
    }

    private Widget createLanguagesPanel() {
        Button submit = new Button(world.messages().submit());
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserLanguages(userChooser.getSelectedUser().getId(), languagesBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.add(new HTML(world.messages().languages()));
        panel.add(languagesBox);
        panel.add(submit);
        return panel;
    }

    private Widget createProjectIdPanel() {
        Button submit = new Button(world.messages().submit());
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserProjectId(userChooser.getSelectedUser().getId(), projectIdBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.add(new HTML(world.messages().projectId()));
        panel.add(projectIdBox);
        panel.add(submit);
        return panel;
    }

    private Widget createProviderIdPanel() {
        Button submit = new Button(world.messages().submit());
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserProviderId(userChooser.getSelectedUser().getId(), providerIdBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.add(new HTML(world.messages().providerId()));
        panel.add(providerIdBox);
        panel.add(submit);
        return panel;
    }

}
