package com.optimised.cylonbackup.views.sites;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.FileTools;
import com.optimised.cylonbackup.tools.IniFunctions;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Log4j2
@RolesAllowed({"SUPERVISOR"})
@PageTitle("Sites")
@Route(value = "store-number", layout = MainLayout.class)

public class SitesView extends VerticalLayout {
  final static Marker DB = MarkerManager.getMarker("DB");
  private final SettingService settingService;
  Grid<Site> grid = new Grid<>(Site.class);
  TextField filterByName = new TextField();
  IntegerField filterByStoreNo = new IntegerField();
  Button updateSites = new Button("Update Sites");
  SitesForm form;
  SiteService siteService;
  final IniFunctions iniFunctions;

  public SitesView(SiteService siteService, IniFunctions iniFunctions, SettingService settingService) {
    this.siteService = siteService;
    this.iniFunctions = iniFunctions;
    addClassName("list-view");
    setSizeFull();
    configureGrid();
    configureForm();
    add(getToolbar(), getContent());
    updateList();
    closeEditor();
    this.settingService = settingService;
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
    form = new SitesForm();
    form.setWidth("25em");
    form.addSaveListener(this::saveSite);
    form.addResetReadOnlySitesListener(this::resetReadOnlySites);
    form.addCloseListener(e -> closeEditor());
  }

  private void saveSite(SitesForm.SaveEvent event) {
    siteService.saveSite(event.getSite());
    updateList();
    closeEditor();
  }

  private void resetReadOnlySites(SitesForm.ResetReadOnlySitesEvent event) {
    String sitePath = settingService.getSetting().getCylonPath() + "/" + event.getSite().getDirectory();
    FileTools.setFilesReadOnly(sitePath,true);
    Notification.show("Site "+ event.getSite().getName() + " read only has been reset");
    log.info("Site {} read only has been reset", event.getSite().getName());
  }

  private void configureGrid() {
    CommonObjects.setSiteGrid(grid);
    grid.asSingleSelect().addValueChangeListener(event -> editSite(event.getValue()));
  }

  private HorizontalLayout getToolbar() {
    CommonObjects.setFilters(filterByName,filterByStoreNo);
    filterByName.addValueChangeListener(e -> updateList());
    filterByStoreNo.addValueChangeListener(e -> updateList());

    updateSites.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    updateSites.addClickListener(event -> {
      IniFunctions.update = true;
      Notification notification = Notification.show("Updating.. This may take a few minutes");
      notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
      iniFunctions.ManualUpdate();
      while (IniFunctions.update){
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          log.error(DB,e.getMessage());
        }
      }
      updateList();
      Notification.show("Updating.. Complete");
    });

    var toolbar = new HorizontalLayout(filterByStoreNo, filterByName, updateSites);
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
    if (!filterByStoreNo.isEmpty()) {
      if (siteService.searchStoreNumberAndExisting(filterByStoreNo.getValue()).isPresent())
        grid.setItems(siteService.searchStoreNumberAndExisting(filterByStoreNo.getValue()).get());
    } else {
      if (siteService.searchSiteNameAndExisting(filterByName.getValue()).isPresent()) {
        grid.setItems(siteService.searchSiteNameAndExisting(filterByName.getValue()).get());
      }
    }
  }
}
