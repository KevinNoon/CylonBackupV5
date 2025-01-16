package com.optimised.cylonbackup.views.backup;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.optimised.cylonbackup.tools.FileTools;
import com.optimised.cylonbackup.tools.IniFunctions;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Log4j2
@PageTitle("Restore")
@Route(value = "restore", layout = MainLayout.class)
@RolesAllowed({"SUPERVISOR"})
@Uses(Icon.class)
public class RestoreView extends VerticalLayout {

    Grid<Site> grid = new Grid<>(Site.class);
    TextField filterByName = new TextField();
    IntegerField filterByStoreNo = new IntegerField();
    RestoreForm form;
    Button restore = new Button("Restore Selected");
    Checkbox singleSite = new Checkbox("SingleSite");
    Button restoreFromFileButton = new Button("Restore from file");
    Button uploadButton = new Button("Restore from File");

    final BackupTools backupTools;
    final SiteService siteService;
    final SettingService settingService;
    final IniFunctions iniFunctions;
    final static Marker DB = MarkerManager.getMarker("DB");

    public RestoreView(BackupTools backupTools, SiteService siteService, SettingService settingService, IniFunctions iniFunctions) {
        this.backupTools = backupTools;
        this.siteService = siteService;
        this.settingService = settingService;
        this.iniFunctions = iniFunctions;
        addClassName("list-view");
        setSizeFull();
        configureForm();
        configureGrid();
        add(getTopToolbar(), getContent());
        updateList();
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
        form = new RestoreForm(backupTools);
        form.setWidth("25em");
        form.setVisible(false);
        form.addCloseListener(e -> closeEditor());
    }

    private void configureGrid() {
        CommonObjects.setSiteGrid(grid);
        grid.asSingleSelect().addValueChangeListener(event -> editSite(event.getValue()));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

    }

    public void editSite(Site site) {
        if (site == null) {
            form.setVisible(false);
            closeEditor();
        } else {
            List<String> backupList = FileTools.getSiteBackupNames(
                Path.of(settingService.getSetting().getBackupPath()), site, true);
            if (backupList.isEmpty()) {
                Notification notification = Notification.show("No backups available");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                closeEditor();
            } else {
                form.setBackups(backupList);
                form.setVisible(true);
                addClassName("editing");
            }
        }
    }

    private void closeEditor() {
        form.setVisible(false);
        form.setBackups(null);
        removeClassName("editing");
    }

    private HorizontalLayout getTopToolbar() {
        singleSite.addClickListener(c -> {
            if (singleSite.getValue()) {
                grid.setSelectionMode(Grid.SelectionMode.SINGLE);
                restore.setEnabled(false);
            } else {
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
                restore.setEnabled(true);
            }
        });
        restore.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        restore.addClickListener(c -> {
            Set<Site> selectedSites = grid.getSelectedItems();
            List<List<String>> selectedBackups = selectedSites.stream().map(s -> FileTools.getSiteBackupNames(
                Path.of(settingService.getSetting().getBackupPath()), s, false)).toList();
            List<String> backups = new ArrayList<>();
            boolean noBackup = false;
            for (List<String> s : selectedBackups) {
                if (!s.isEmpty()) {
                    backups.add(s.get(0));
                } else {
                    noBackup = true;
                }
            }
            if (noBackup) {
                Notification notification = Notification.show("No backups available for some site(s)");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            backupTools.RestoreSites(backups,false);
        });
        restoreFromFileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        restoreFromFileButton.addClickListener(c -> {
        });
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        int maxFileSizeInBytes = 10 * 1024 * 1024;
        Upload upload = new Upload(memoryBuffer);
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setAcceptedFileTypes(".ccb","application/zip");
        upload.setDropAllowed(false);

        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        upload.addSucceededListener(event -> {
            File file = new File("Upload.zip");
            try(OutputStream os = new FileOutputStream(file)) {
                os.write(memoryBuffer.getInputStream().readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> backup = List.of(file.getAbsolutePath());
            backupTools.RestoreSites(backup,false);
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        topBarBackup.add(upload,restore,singleSite);
        topBarBackup.setAlignItems(FlexComponent.Alignment.END);
        topBar.add(topBarFilter,topBarBackup);
        topBar.getThemeList().add("spacing-xl");
        topBar.addClassName("toolbar");
        return topBar;
    }

    private void updateList() {
        //grid.setItems(FileTools.getBackedUpSites());
        if (!filterByStoreNo.isEmpty()) {
            if (siteService.searchStoreNumberAndNotCheckedOut(filterByStoreNo.getValue()).isPresent()) {
                grid.setItems(siteService.searchStoreNumberAndNotCheckedOut(filterByStoreNo.getValue()).get());
            }
        } else {
            if (siteService.searchSiteNameAndNotCheckedOut(filterByName.getValue()).isPresent()) {
                grid.setItems(siteService.searchSiteNameAndNotCheckedOut(filterByName.getValue()).get());
            }
        }
    }
}
