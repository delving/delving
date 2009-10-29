package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Grid;
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
 *         <p/>
 *         modified by Nicola
 */

public class UsersWidget extends DashboardWidget {
    private CheckBox enabledBox;
    private ListBox roleBox;
    private UserChooser userChooser;
    private TextBox userNameBox = new TextBox();
    private TextBox emailBox = new TextBox();
    private TextBox languagesBox = new TextBox();
    private TextBox projectIdBox = new TextBox();
    private TextBox providerIdBox = new TextBox();
    private VerifyDialog verifyDialog;

    public UsersWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        verifyDialog = new VerifyDialog(world.messages());
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(10);
        vp.add(createRoleBox());
        vp.add(createEnabledBox());
        vp.add(createFieldForm());
        vp.add(createDeleteButton());
        DecoratorPanel fields = new DecoratorPanel();
        fields.setWidget(vp);
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(createUserChooser());
        hp.add(fields);
        return hp;
    }

    private Widget createUserChooser() {
        userChooser = new UserChooser(world);
        userChooser.setListener(new UserChooser.Listener() {
            public void userSelected(UserX user) {
                enabledBox.setValue(user.isEnabled());
                roleBox.setSelectedIndex(user.getRole().ordinal());
                userNameBox.setValue(user.getUserName());
                emailBox.setValue(user.getEmail());
                languagesBox.setText(user.getLanguages());
                projectIdBox.setText(user.getProjectId());
                providerIdBox.setText(user.getProviderId());
            }
        });
        return userChooser.getWidget();
    }

    private Widget createRoleBox() {
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
        return roleBox;
    }

    private Widget createEnabledBox() {
        enabledBox = new CheckBox(world.messages().userEnabled());
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
        return enabledBox;
    }

    private Widget createDeleteButton() {
        final Button deleteButton = new Button(world.messages().deleteThisUser());
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
        return deleteButton;
    }

    private Widget createFieldForm() {
        Button submitLanguage = new Button(world.messages().submit());
        submitLanguage.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserLanguages(userChooser.getSelectedUser().getId(), languagesBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        Button submitProjectId = new Button(world.messages().submit());
        submitProjectId.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserProjectId(userChooser.getSelectedUser().getId(), projectIdBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        Button submitProviderId = new Button(world.messages().submit());
        submitProviderId.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent sender) {
                world.service().setUserProviderId(userChooser.getSelectedUser().getId(), providerIdBox.getText(), new Reply<Void>() {
                    public void onSuccess(Void result) {
                        // todo: update the user object contents, back to normal appearance
                    }
                });
            }
        });
        Button submitUserName = new Button(world.messages().submit());
        submitUserName.setEnabled(false);
        Button submitEmail = new Button(world.messages().submit());
        submitEmail.setEnabled(false);
        Grid grid = new Grid(5, 3);
        grid.setWidget(0, 0, new HTML(world.messages().userName()));
        grid.setWidget(0, 1, userNameBox);
        grid.setWidget(0, 2, submitUserName);
        grid.setWidget(1, 0, new HTML(world.messages().emailAddress()));
        grid.setWidget(1, 1, emailBox);
        grid.setWidget(1, 2, submitEmail);
        grid.setWidget(2, 0, new HTML(world.messages().languages()));
        grid.setWidget(2, 1, languagesBox);
        grid.setWidget(2, 2, submitLanguage);
        grid.setWidget(3, 0, new HTML(world.messages().projectId()));
        grid.setWidget(3, 1, projectIdBox);
        grid.setWidget(3, 2, submitProjectId);
        grid.setWidget(4, 0, new HTML(world.messages().providerId()));
        grid.setWidget(4, 1, providerIdBox);
        grid.setWidget(4, 2, submitProviderId);
        return grid;
    }
}
