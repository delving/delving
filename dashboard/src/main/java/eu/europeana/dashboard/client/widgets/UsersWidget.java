package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
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
    private ListBox roleBox;
    private UserChooser userChooser;
    private UserX user;
    private TextBox userNameBox = new TextBox();
    private TextBox emailBox = new TextBox();
    private TextBox passwordBox = new PasswordTextBox();
    private TextBox firstNameBox = new TextBox();
    private TextBox lastNameBox = new TextBox();
    private TextBox languagesBox = new TextBox();
    private TextBox projectIdBox = new TextBox();
    private TextBox providerIdBox = new TextBox();
    private CheckBox newsletterBox = new CheckBox();
    private CheckBox enabledBox = new CheckBox();
    private CheckBox deleteBox = new CheckBox();
    private Button submitButton = new Button(world.messages().updateThisUser());
    private Button newUserButton = new Button(world.messages().createUser());
    private VerifyDialog verifyDialog;

    public UsersWidget(World world) {
        super(world);
    }

    @Override
    protected Widget createWidget() {
        verifyDialog = new VerifyDialog(world.messages());
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(20);
        vp.add(createFieldForm());
        vp.add(createSubmitButton());
        DecoratorPanel fields = new DecoratorPanel();
        fields.setWidget(vp);
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(createUserPanel());
        hp.add(fields);
        setUser(null);
        return hp;
    }

    private Widget createUserPanel() {
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(20);
        vp.add(createUserChooser());
        vp.add(createNewUserButton());
        return vp;
    }

    private Widget createUserChooser() {
        userChooser = new UserChooser(world);
        userChooser.setListener(new UserChooser.Listener() {

        	@Override
            public void userSelected(UserX user) {
                setUser(user);
            }
        });
        return userChooser.getWidget();
    }

    private Widget createNewUserButton() {
        newUserButton.setWidth("100%");
        newUserButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserX user = new UserX();
                user.setRole(RoleX.ROLE_USER);
                user.setUserName("?");
                user.setEmail("?");
                setUser(user);
            }
        });
        return newUserButton;
    }

    private Widget createRoleBox() {
        roleBox = new ListBox();
        for (RoleX role : RoleX.values()) {
            if (world.user().getRole() != RoleX.ROLE_GOD && role == RoleX.ROLE_GOD) {
                continue; // cannot switch anybody to god role unles you are god
            }
            roleBox.addItem(role.toString());
        }
        return roleBox;
    }

    private Widget createSubmitButton() {
        submitButton.setWidth("100%");
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (deleteBox.getValue()) {
                    verifyDialog.ask(submitButton, world.messages().deleteCaption(), world.messages().deleteThisUserQuestion(), new Runnable() {
                        @Override
                        public void run() {
                            world.service().removeUser(userChooser.getSelectedUser(), new Reply<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    setUser(null);
                                }
                            });
                        }
                    });
                }
                else {
                    transferFieldsToUser();
                    world.service().updateUser(user, new Reply<UserX>() {
                        @Override
                        public void onSuccess(UserX user) {
                            setUser(null);
                        }
                    });
                }
            }
        });
        return submitButton;
    }

    private Widget createFieldForm() {
        Grid grid = new Grid(12, 2);
        grid.setWidget(0, 0, new HTML(world.messages().userName()));
        grid.setWidget(0, 1, userNameBox);
        grid.setWidget(1, 0, new HTML(world.messages().emailAddress()));
        grid.setWidget(1, 1, emailBox);
        grid.setWidget(2, 0, new HTML(world.messages().firstName()));
        grid.setWidget(2, 1, firstNameBox);
        grid.setWidget(3, 0, new HTML(world.messages().lastName()));
        grid.setWidget(3, 1, lastNameBox);
        grid.setWidget(4, 0, new HTML(world.messages().languages()));
        grid.setWidget(4, 1, languagesBox);
        grid.setWidget(5, 0, new HTML(world.messages().projectId()));
        grid.setWidget(5, 1, projectIdBox);
        grid.setWidget(6, 0, new HTML(world.messages().providerId()));
        grid.setWidget(6, 1, providerIdBox);
        grid.setWidget(7, 0, new HTML(world.messages().newsletter()));
        grid.setWidget(7, 1, newsletterBox);
        grid.setWidget(8, 0, new HTML(world.messages().rolePrompt()));
        grid.setWidget(8, 1, createRoleBox());
        grid.setWidget(9, 0, new HTML(world.messages().userEnabled()));
        grid.setWidget(9, 1, enabledBox);
        grid.setWidget(10, 0, new HTML(world.messages().setPassword()));
        grid.setWidget(10, 1, passwordBox);
        grid.setWidget(11, 0, new HTML(world.messages().deleteThisUser()));
        grid.setWidget(11, 1, deleteBox);
        return grid;
    }

    private void setUser(UserX user) {
        this.user = user;
        boolean empty = user == null;
        if (empty) {
            userChooser.clear();
        }
        // contents
        roleBox.setSelectedIndex(empty ? 0 : user.getRole().ordinal());
        userNameBox.setValue(empty ? "" : user.getUserName());
        emailBox.setValue(empty ? "" : user.getEmail());
        passwordBox.setValue("");
        firstNameBox.setValue(empty ? "" : user.getFirstName());
        lastNameBox.setValue(empty ? "" : user.getLastName());
        languagesBox.setText(empty ? "" : user.getLanguages());
        projectIdBox.setText(empty ? "" : user.getProjectId());
        providerIdBox.setText(empty ? "" : user.getProviderId());
        newsletterBox.setValue(!empty && user.isNewsletter());
        enabledBox.setValue(!empty && user.isEnabled());
        deleteBox.setValue(false);
        // enablement
        roleBox.setEnabled(!empty);
        userNameBox.setEnabled(!empty);
        emailBox.setEnabled(!empty);
        passwordBox.setEnabled(!empty);
        firstNameBox.setEnabled(!empty);
        lastNameBox.setEnabled(!empty);
        languagesBox.setEnabled(!empty);
        projectIdBox.setEnabled(!empty);
        providerIdBox.setEnabled(!empty);
        newsletterBox.setEnabled(!empty);
        enabledBox.setEnabled(!empty);
        deleteBox.setEnabled(!empty);
        submitButton.setEnabled(!empty);
    }

    private void transferFieldsToUser() {
        user.setEnabled(enabledBox.getValue());
        user.setRole(RoleX.values()[roleBox.getSelectedIndex()]);
        user.setUserName(userNameBox.getValue().trim());
        user.setEmail(emailBox.getValue().trim());
        user.setPassword(passwordBox.getValue());
        user.setFirstName(firstNameBox.getValue().trim());
        user.setLastName(lastNameBox.getValue().trim());
        user.setLanguages(languagesBox.getValue().trim());
        user.setProjectId(projectIdBox.getValue());
        user.setProviderId(providerIdBox.getValue());
        user.setNewsletter(newsletterBox.getValue());
        user.setEnabled(enabledBox.getValue());
    }
}
