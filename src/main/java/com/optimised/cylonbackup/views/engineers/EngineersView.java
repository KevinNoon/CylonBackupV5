package com.optimised.cylonbackup.views.engineers;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.repository.SiteRepo;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.security.AuthenticatedUser;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@RolesAllowed("SUPERVISOR")
@PageTitle("Engineers")
@Route(value = "engineers", layout = MainLayout.class)
public class EngineersView extends VerticalLayout {

    private final SiteService siteService;
    Grid<Engineer> grid= new Grid<>(Engineer.class);
    EngineerForm form;
    EngineerService engineerService;
    AuthenticatedUser authenticatedUser;

    public EngineersView(EngineerService engineerService, SiteService siteService){
        this.engineerService = engineerService;
        this.siteService = siteService;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(),getContent());
        updateList();
        closeEditor();

    }

    private HorizontalLayout getToolbar() {

        Button addEngineerButton = new Button("Add Engineer");
        addEngineerButton.addClickListener(click -> addEngineer());
        var toolbar = new HorizontalLayout(addEngineerButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void addEngineer() {
        grid.asSingleSelect().clear();
        editEngineer(new Engineer());
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
        form = new EngineerForm();
        form.setWidth("25em");
        form.addSaveListener(this::saveEngineer);
        form.addDeleteListener(this::deleteEngineer);
        form.addCloseListener(e -> closeEditor());
    }

    private void saveEngineer(EngineerForm.SaveEvent event) {
        engineerService.saveEngineer(event.getEngineer());
        updateList();
        closeEditor();
    }

    private void deleteEngineer(EngineerForm.DeleteEvent event) {
        //Check if engineer has any sites checked out.
            Optional<Site> site = siteService.searchSiteByEngineer(event.getEngineer());
            if (site.isPresent()) {
                Notification.show("Delete Engineer: " + event.getEngineer().getFullName() +
                    " not possible as they have this site checked out: " + site.get().getName());
            } else {
                engineerService.delete(event.getEngineer().getId());
                updateList();
                closeEditor();
            }
    }

    private void configureGrid(){
        grid.addClassNames("engineer-grid");
        grid.setSizeFull();
        grid.setColumns("forename","lastname","email");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editEngineer(event.getValue()));
    }

    public void editEngineer(Engineer engineer) {
        if (engineer == null) {
            closeEditor();
        } else {
            form.setEngineer(engineer);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setEngineer(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(engineerService.findAllEngineers());
    }
}
