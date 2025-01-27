package com.optimised.cylonbackup.views.backup;
import com.optimised.cylonbackup.data.entity.Setting;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalTime;

@PageTitle("Backup")
@Route(value = "backup", layout = MainLayout.class)
@RolesAllowed({"SUPERVISOR"})
@Uses(Icon.class)
public class BackupView extends VerticalLayout {

    Grid<Site> grid= new Grid<>(Site.class);
    TextField filterByName = new TextField();
    IntegerField filterByStoreNo = new IntegerField();
    Button backupButton = new Button("Backup");
    TimePicker timePicker = new TimePicker("Auto backup time");
    Checkbox checkBox = new Checkbox();

    final SiteService siteService;
    final SettingService settingService;
    final BackupTools backupTools;


    @Autowired
    public BackupView(SiteService siteService, SettingService settingService, BackupTools backupTools){
        this.siteService = siteService;
        this.settingService = settingService;
        this.backupTools = backupTools;

        addClassName("list-view");
        setSizeFull();
        configureGrid();
        add(getTopToolbar(), getContent());
        updateList();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid(){
        CommonObjects.setSiteGrid(grid);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private HorizontalLayout getTopToolbar() {
        backupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backupButton.addClickListener(clickEvent -> {
                Notification.show("Backup started");
                grid.getSelectedItems().forEach(backupTools::Backup);
                Notification.show("Backup completed");
            }
        );
        timePicker.setValue(LocalTime.of(1, 0));
        timePicker.setStep(Duration.ofMinutes(15));
        timePicker.setValue(settingService.getSetting().getBackupTime());
        timePicker.addValueChangeListener(timePickerLocalTimeComponentValueChangeEvent -> {
            Setting setting = settingService.getSetting();
            setting.setBackupTime(timePicker.getValue());
            settingService.saveSetting(setting);
        });
        checkBox.setLabel("Auto backup");
        checkBox.setValue(settingService.getSetting().getAutoBackup());
        checkBox.addClickListener(checkboxClickEvent -> {
            Setting setting = settingService.getSetting();
            setting.setAutoBackup(checkBox.getValue());
            settingService.saveSetting(setting);
        });
        CommonObjects.setFilters(filterByName,filterByStoreNo);
        filterByName.addValueChangeListener(e -> updateList());
        filterByStoreNo.addValueChangeListener(e -> updateList());

        HorizontalLayout topBar = new HorizontalLayout();
        HorizontalLayout topBarFilter = new HorizontalLayout();
        HorizontalLayout topBarBackup = new HorizontalLayout();
        topBarFilter.add(filterByName);
        topBarFilter.add(filterByStoreNo);
        topBarFilter.setAlignItems(FlexComponent.Alignment.END);
        topBarBackup.add(backupButton);
        topBarBackup.add(timePicker);
        topBarBackup.add(checkBox);
        topBarBackup.setAlignItems(FlexComponent.Alignment.END);
        topBar.add(topBarFilter,topBarBackup);
        topBar.getThemeList().add("spacing-xl");
        topBar.addClassName("toolbar");
        return topBar;
    }

    private void updateList() {
        if (!filterByStoreNo.isEmpty()){
            if (siteService.searchStoreNumberAndNotCheckedOutAndExisting(filterByStoreNo.getValue()).isPresent()) {
                grid.setItems(siteService.searchStoreNumberAndNotCheckedOutAndExisting(filterByStoreNo.getValue()).get());
            }
        } else {
            if (siteService.searchSiteNameAndNotCheckedOutAndExisting(filterByName.getValue()).isPresent()) {
                grid.setItems(siteService.searchSiteNameAndNotCheckedOutAndExisting(filterByName.getValue()).get());
            }
        }
    }
}
