
package com.optimised.cylonbackup.views.checkout;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.EmailService;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

@RolesAllowed({"SUPERVISOR"})
@PageTitle("Check in")
@Route(value = "checkin", layout = MainLayout.class)
public class CheckinView extends VerticalLayout {
    Grid<Site> grid = new Grid<>(Site.class);
    TextField filterByName = new TextField();
    IntegerField filterByStoreNo = new IntegerField();
    SiteService siteService;
    SettingService settingService;
    EngineerService engineerService;
    EmailService emailService;
    final BackupTools backupTools;
    CheckinForm form;

    private static final Logger log = LogManager.getLogger(CheckinView.class);
    static final Marker DB = MarkerManager.getMarker("DB");

    @Autowired
    public CheckinView(SiteService siteService, SettingService settingService, EngineerService engineerService, EmailService emailService, BackupTools backupTools) {
        this.siteService = siteService;
        this.settingService = settingService;
        this.engineerService = engineerService;
        this.emailService = emailService;
        this.backupTools = backupTools;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2.0, grid);
        content.setFlexGrow(1.0, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new CheckinForm(emailService, engineerService, siteService, backupTools,settingService);
        form.setWidth("25em");
        form.addCheckoutListener(this::saveSite);
        form.addCancelListener(e -> closeEditor());
    }

    private void saveSite(CheckinForm.CheckoutEvent event) {
        siteService.saveSite(event.getSite());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("checkin-grid");
        grid.setSizeFull();
        grid.setColumns("storeNumber", "name", "siteNumber", "engineer.fullName");
        grid.getColumns().get(3).setHeader("Engineer");
        grid.getColumns().forEach((col) -> {col.setAutoWidth(true);});
        grid.asSingleSelect().addValueChangeListener((event) -> {editSite((Site)event.getValue());});
    }

    private HorizontalLayout getToolbar() {
        filterByName.setPlaceholder("Filter by name...");
        filterByName.setClearButtonVisible(true);
        filterByName.setValueChangeMode(ValueChangeMode.LAZY);
        filterByName.addValueChangeListener((e) -> {updateList();});
        filterByStoreNo.setPlaceholder("Filter by store no...");
        filterByStoreNo.setClearButtonVisible(true);
        filterByStoreNo.setValueChangeMode(ValueChangeMode.LAZY);
        filterByStoreNo.addValueChangeListener((e) -> {updateList();});
        String backupPath = settingService.getSetting().getBackupPath();
        File theDir = new File(backupPath);
        if (!theDir.exists()) {
            if (!theDir.mkdirs()){
                log.error(DB, "Failed to create backup directory");
            }
        }

        HorizontalLayout toolbar = new HorizontalLayout(filterByStoreNo, filterByName);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editSite(Site site) {
        if (site == null) {
            closeEditor();
        } else {
            form.setSite(site);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setSite(null);
        form.setVisible(false);
        removeClassName("editing");
        updateList();
    }

    private void updateList() {
        Optional<ArrayList<Site>> sites;
        if (!filterByStoreNo.isEmpty()) {
            sites = siteService.searchStoreNumberAndCheckedOutAndExisting(filterByStoreNo.getValue());
            sites.ifPresent((siteArrayList) -> {
                grid.setItems(siteArrayList);
            });
        } else {
            System.out.println(filterByName.getValue());
            sites = siteService.searchSiteNameAndCheckedOutAndExisting(filterByName.getValue());
            sites.ifPresent((siteArrayList) -> {
                grid.setItems(siteArrayList);
            });
        }

    }
}

