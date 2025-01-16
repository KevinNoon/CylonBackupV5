package com.optimised.cylonbackup.views.checkout;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.EmailService;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
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

@RolesAllowed("ENGINEER")
@PageTitle("Check out")
@Route(value = "checkout", layout = MainLayout.class)
public class CheckoutView extends VerticalLayout {


    Grid<Site> grid= new Grid<>(Site.class);
    TextField filterByName = new TextField();
    IntegerField filterByStoreNo = new IntegerField();

     SiteService siteService;
     SettingService settingService;
     EngineerService engineerService;
     EmailService emailService;
     final BackupTools backupTools;
    CheckoutForm form;

    private static final Logger log = LogManager.getLogger(CheckoutView.class);
    static final Marker DB = MarkerManager.getMarker("DB");

    @Autowired
    public CheckoutView(SiteService siteService, SettingService settingService, EngineerService engineerService, EmailService emailService, BackupTools backupTools){
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
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new CheckoutForm(emailService, engineerService, siteService, backupTools,settingService);
        form.setWidth("25em");
        form.addCheckoutListener(this::saveSite);
        form.addCancelListener(e -> closeEditor());
    }

    private void saveSite(CheckoutForm.CheckoutEvent event) {
        siteService.saveSite(event.getSite());
        updateList();
        closeEditor();
    }

    private void configureGrid(){
        grid.addClassNames("checkout-grid");
        grid.setSizeFull();
        grid.setColumns("storeNumber","name","siteNumber");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editSite(event.getValue()));

    }

    private HorizontalLayout getToolbar() {
        CommonObjects.setFilters(filterByName, filterByStoreNo);
        filterByName.addValueChangeListener(e -> updateList());
        filterByStoreNo.addValueChangeListener(e -> updateList());

        String backupPath = settingService.getSetting().getBackupPath();
        File theDir = new File(backupPath);
        if (!theDir.exists()) {
            if (!theDir.mkdirs()){
                log.error(DB, "Failed to create backup directory");
            }
        }
        var toolbar = new HorizontalLayout(filterByStoreNo,filterByName);
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
    }

    private void updateList() {
        Optional<ArrayList<Site>> sites;
        if (!this.filterByStoreNo.isEmpty()) {
            sites = siteService.searchStoreNumberAndNotCheckedOutAndExisting((Integer) filterByStoreNo.getValue());
            sites.ifPresent((siteArrayList) -> {grid.setItems(siteArrayList);
            });
        } else {
            sites = siteService.searchSiteNameAndNotCheckedOutAndExisting(filterByName.getValue());
            sites.ifPresent((siteArrayList) -> {grid.setItems(siteArrayList);
            });
        }
    }
}