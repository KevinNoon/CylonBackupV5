package com.optimised.cylonbackup.views.users;

import com.optimised.cylonbackup.data.Role;
import com.optimised.cylonbackup.data.entity.User;
import com.optimised.cylonbackup.security.AuthenticatedUser;
import com.optimised.cylonbackup.security.SecurityConfiguration;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.engineers.EngineerForm;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.util.Optional;
public class UserForm extends FormLayout {

    public void setUser(User user) {
        binder.setBean(user);
    }

    TextField name = new TextField("Name");
    TextField username = new TextField("User Name");
    PasswordField password1 = new PasswordField("Password");
    PasswordField password2 = new PasswordField("Confirm Password");
    CheckboxGroup<Role> roles = new CheckboxGroup<>();
    Checkbox isdarkmode = new Checkbox("Dark Mode");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button cancel = new Button("Cancel");

    Boolean newUser;

    Binder<User> binder = new BeanValidationBinder<>(User.class);
    //private final UserService userService;
    private final SecurityConfiguration securityConfiguration;
    private final AuthenticatedUser authenticatedUser;
    public UserForm(SecurityConfiguration securityConfiguration, AuthenticatedUser authenticatedUser) {
     //   this.userService = userService;
        this.securityConfiguration = securityConfiguration;
        this.authenticatedUser = authenticatedUser;
        addClassName("user-form");
        roles.setLabel("Roles");
        roles.setItems(Role.values());
        roles.setItemLabelGenerator(Role::getLabel);

        //roles.setItems(Role.ADMIN,Role.SUPERVISOR, Role.ENGINEER, Role.USER);

        binder.bindInstanceFields(this);
        add(
                name,
                username,
                password1,
                password2,
                roles,
                isdarkmode,
                createButtonsLayout());
    }


    private HorizontalLayout createButtonsLayout() {
        CommonObjects.setButtonsSaveDeleteCancel(save, cancel, delete);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> validateAndDelete());
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save,delete, cancel);
    }

    private void validateAndSave() {
        newUser = binder.getBean().getId() == null;

        if (binder.isValid()) {
            User user = binder.getBean();
            String p1 = password1.getValue().trim();
            String p2 = password2.getValue().trim();

            if (name.getValue().trim().isEmpty()) {
                Notification.show("Name must not be empty");
            } else if (username.getValue().trim().isEmpty()) {
                Notification.show("User name must not be empty");
            }


            else if ((p1.isEmpty()) && (user.getId() == null)) {
                Notification.show("Password must not be empty");
            }
            else if ((p1.length() < 8) && (user.getId() == null)) {
                Notification.show("Password must have a minimum of 8 characters");
            }
            else if ((!p1.equals(p2)) && (user.getId() == null)) {
                Notification.show("Passwords much match");
            }
            else if (!password1.isEmpty() && (p1.length() < 8)){
                    Notification.show("Password must have a minimum of 8 characters");
            }
            else if (!password1.isEmpty() && !p1.equals(p2)){
                    Notification.show("Passwords much match");
            }
            else if (roles.getSelectedItems().isEmpty()) {
                Notification.show("User must have at least one role");
            }
                else {
                 if (!p1.isEmpty()) {
                     String passwordHash = this.securityConfiguration.passwordEncoder().encode(p1);
                     user.setHashedPassword(passwordHash);
                 }
                System.out.println(binder.getBean().getName());
                fireEvent(new SaveEvent(this, binder.getBean()));
            }
        }
    }

    private void validateAndDelete(){
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirm Deletion");
        confirmDialog.setText("Are you sure you want to delete this user?");;
        confirmDialog.setConfirmButton("Delete", event -> {
            fireEvent(new DeleteEvent(this,binder.getBean()));
        });
        confirmDialog.setCancelButton("Cancel", event -> fireEvent(new CloseEvent(this)));

        if (binder.getBean().getId() != null && binder.isValid()){
            System.out.println(authenticatedUser.get());
            Optional<User> maybeUser = authenticatedUser.get();
            if (maybeUser.isPresent()){
                if (!maybeUser.get().getUsername().equals(binder.getBean().getUsername())){
                    confirmDialog.open();
                } else {
                    Notification.show("Can not delete yourself");
                }

            }
        }
    }

    public static abstract class UserFormEvent extends ComponentEvent<UserForm> {
        private User user;

        protected UserFormEvent(UserForm source, User user) {
            super(source, false);

            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    public static class SaveEvent extends UserFormEvent {
        SaveEvent(UserForm source, User user) {
            super(source, user);
        }
    }

    public static class DeleteEvent extends UserFormEvent {
        DeleteEvent(UserForm source, User user) {
            super(source, user);
        }

    }

    public static class CloseEvent extends UserFormEvent {
        CloseEvent(UserForm source) {
            super(source, null);
        }
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
