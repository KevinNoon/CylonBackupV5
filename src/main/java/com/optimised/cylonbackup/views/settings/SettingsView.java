package com.optimised.cylonbackup.views.settings;

import com.optimised.cylonbackup.data.entity.Setting;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;


@RolesAllowed({"ADMIN"})
@PageTitle("General Settings")
@Route(value = "settings", layout = MainLayout.class)

public class SettingsView extends VerticalLayout {

  final SettingService settingService;

  TextField cylonPath = new TextField("Cylon Path");
  TextField backupPath = new TextField("Backup Path");
  Checkbox autoBackup = new Checkbox("Auto Backup");
  Checkbox exBackups = new Checkbox("Backups");
  Checkbox exDrawings = new Checkbox("Drawings");
  Checkbox inAlarmsDB = new Checkbox("Alarms DB");
  Checkbox inPasswords = new Checkbox("Passwords");
  Checkbox inReports = new Checkbox("Reports");
  Checkbox inWN3000ini = new Checkbox("WN3000ini");
  TimePicker backupTime = new TimePicker("Backup Time");
  Checkbox autoUpdateSites = new Checkbox("Auto Update Sites");
  TimePicker updateSitesTime = new TimePicker("Update Sites Time");
  Button save = new Button("Save");
  Button cancel = new Button("Cancel");

  Binder<Setting> binder = new BeanValidationBinder<>(Setting.class);

  public void setSettings(Setting setting){
    binder.setBean(setting);
  }

  public SettingsView(SettingService settingService){
    binder.bindInstanceFields(this);
    this.settingService = settingService;
    setSettings(settingService.getSetting());
    createLayout();
    updateValues();
    add(cylonPath,backupPath,backupTime,autoBackup,createExclude(),createInclude(),updateSitesTime,autoUpdateSites, createButtonsLayout());
  }

  private void   createLayout() {
    cylonPath.setWidth("25%");
    cylonPath.setPlaceholder("Cylon Path");
    cylonPath.setRequired(true);
    backupPath.setWidth("25%");
    backupPath.setPlaceholder("Backup Path");
    backupPath.setRequired(true);
    backupTime.setWidth("25%");
    backupTime.setPlaceholder("Update Time");
    backupTime.setRequired(true);
    autoBackup.setWidth("25%");
    autoUpdateSites.setWidth("25%");
    updateSitesTime.setWidth("25%");
    updateSitesTime.setPlaceholder("Update Sites Time");
    updateSitesTime.setRequired(true);
  }

  private VerticalLayout createExclude(){
    VerticalLayout vLayout = new VerticalLayout();
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.add(exBackups,exDrawings);
    H4 title = new H4("Exclude Files");
    vLayout.add(title, hLayout);
    return vLayout;
  }

  private VerticalLayout createInclude(){
    VerticalLayout vLayout = new VerticalLayout();
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.add(inPasswords,inReports,inWN3000ini, inAlarmsDB);
    H4 title = new H4("System Files");
    vLayout.add(title, hLayout);
    return vLayout;
  }


  private void updateValues(){
    Setting setting = settingService.getSetting();
    cylonPath.setValue(setting.getCylonPath());
    backupPath.setValue(setting.getBackupPath());
    backupTime.setValue(setting.getBackupTime());
    autoBackup.setValue(setting.getAutoBackup());
    updateSitesTime.setValue(setting.getUpdateSitesTime());
  }

  private HorizontalLayout createButtonsLayout() {
    CommonObjects.setButtonsSaveCancel(save, cancel);
    save.addClickListener(event -> {validateAndSave();});
    binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
    cancel.addClickListener(event -> {updateValues();});
    return new HorizontalLayout(save,  cancel);
  }

  private void validateAndSave() {
    if (binder.isValid()) {
      settingService.saveSetting(binder.getBean());};
  }
}
