package com.optimised.cylonbackup.views.users;

import com.optimised.cylonbackup.data.entity.User;
import com.optimised.cylonbackup.data.service.UserService;
import com.optimised.cylonbackup.security.AuthenticatedUser;
import com.optimised.cylonbackup.security.SecurityConfiguration;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
@PageTitle("Users")
@Route(value = "users", layout = MainLayout.class)
public class UsersView extends VerticalLayout {

    Grid<User> grid = new Grid<>(User.class);
    UserForm form;
    UserService userService;
    SecurityConfiguration securityConfiguration;
    AuthenticatedUser authenticatedUser;

    public UsersView(UserService userService, SecurityConfiguration securityConfiguration, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.securityConfiguration = securityConfiguration;
        this.authenticatedUser = authenticatedUser;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private HorizontalLayout getToolbar() {

        Button addUser = new Button("Add User");
        addUser.addClickListener(click -> addUser());
        var toolbar = new HorizontalLayout(addUser);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void addUser() {
        grid.asSingleSelect().clear();
        editUser(new User());
    }


    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new UserForm(securityConfiguration, authenticatedUser);
        form.setWidth("25em");
        form.addSaveListener(this::saveUser);
        form.addDeleteListener(this::deleteUser);
        form.addCloseListener(e -> closeEditor());
    }

    private void saveUser(UserForm.SaveEvent event) {
        if ((event.getUser().getId() == null) && (userService.findUserByNameAndUserName(
            event.getUser().getName(),event.getUser().getUsername()).getId() != null)) {
            Notification.show("User already exists");
        } else {
            System.out.println(event.getUser());
            userService.save(event.getUser());
            updateList();
            closeEditor();
        }
    }

    private void deleteUser(UserForm.DeleteEvent event) {
        userService.delete(event.getUser().getId());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("user-grid");
        grid.setSizeFull();
        grid.setColumns("name", "username");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editUser(event.getValue()));
    }

    public void editUser(User user) {
        if (user == null) {
            closeEditor();
        } else {

            form.setUser(user);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.password1.setValue("");
        form.password2.setValue("");
        form.setUser(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(userService.findAllUsers());
    }
}
