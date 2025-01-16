
package com.optimised.cylonbackup.views.checkout;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.EmailService;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import lombok.Generated;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static com.optimised.cylonbackup.views.checkout.CheckinCommon.saveSite;

public class CheckinForm extends FormLayout {
    @Generated
    private static final Logger log = LogManager.getLogger(CheckinForm.class);
    static final Marker DB = MarkerManager.getMarker("DB");

    final EmailService emailService;
    final EngineerService engineerService;
    final SiteService siteService;
    final BackupTools backupTools;
    final SettingService settingService;

    IntegerField storeNumber = new IntegerField("Store Number");
    TextField name = new TextField("Name");
    IntegerField siteNumber = new IntegerField("Site Number");
    DatePicker expectedReturnDate = new DatePicker("Checkin Date");
    ComboBox<Engineer> engineer = new ComboBox<>();
    Button checkout = new Button("Checkout");
    Button checkin = new Button("Checkin");
    Button cancel = new Button("Cancel");
    Binder<Site> binder = new BeanValidationBinder<>(Site.class);

    FileBuffer fileBuffer = new FileBuffer();
    Upload upload = new Upload(fileBuffer);

    public void setSite(Site site) {
        binder.setBean(site);
    }


    public CheckinForm(EmailService emailService, EngineerService engineerService, SiteService siteService, BackupTools backupTools, SettingService settingService) {
        this.emailService = emailService;
        this.engineerService = engineerService;
        this.siteService = siteService;
        this.backupTools = backupTools;
        this.settingService = settingService;

        addClassName("site-form");
        binder.bindInstanceFields(this);
        storeNumber.setReadOnly(true);
        name.setReadOnly(true);

        engineer.setItems(engineerService.findAllEngineers());
        engineer.setItemLabelGenerator(Engineer::getFullName);
        siteNumber.setReadOnly(true);
        expectedReturnDate.setMin(LocalDate.now().plusDays(1L));
        expectedReturnDate.setMax(LocalDate.now().plusDays(14L));
        expectedReturnDate.setValue(LocalDate.now().plusDays(1L));

        add(storeNumber,
            name,
            siteNumber,
            expectedReturnDate,
            engineer,
            createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        checkout.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        checkout.addClickShortcut(Key.ENTER);
        checkout.addClickListener(event -> {
            if (binder.isValid()) {
                fireEvent(new CheckoutEvent(this, binder.getBean()));
            }
        });

        checkin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        //checkinButton.addClickListener(event -> fireEvent(new CheckinEvent(this,binder.getBean())));

        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickShortcut(Key.ESCAPE);
        cancel.addClickListener(event -> {
            fireEvent(new CancelEvent(this));
        });

        binder.addStatusChangeListener((e) -> {
            checkout.setEnabled(binder.isValid());
        });
        binder.addStatusChangeListener((e) -> {
            checkin.setEnabled(binder.isValid());
        });

        int maxFileSizeInBytes = 10485760;
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setAcceptedFileTypes(".ccb", "application/zip", ".zip");
        upload.setDropAllowed(false);
        upload.setUploadButton(checkin);
        upload.addFileRejectedListener((event) -> {
            String errorMessage = event.getErrorMessage();
            Notification notification = Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        upload.addSucceededListener(event -> {
            fireEvent(new CheckinEvent(this, binder.getBean(), event.getUpload()));
        });

        return new HorizontalLayout(checkout, upload, cancel);
    }

    @Getter
    public static abstract class CheckoutFormEvent extends ComponentEvent<CheckinForm> {
        private final Site site;

        protected CheckoutFormEvent(CheckinForm source, Site site) {
            super(source, false);
            this.site = site;
        }
    }

    public class CheckoutEvent extends CheckoutFormEvent {
        CheckoutEvent(CheckinForm source, Site site) {
            super(source, site);
            if (site.getEngineer().getId() != 1L) {
                saveSite(site,siteService,backupTools,emailService,settingService,engineerService,true);
            }
        }
    }

    public class CheckinEvent extends CheckoutFormEvent {
        CheckinEvent(CheckinForm source, Site site, Upload event) {
            super(source, site);

            File uploadedFile = fileBuffer.getFileData().getFile();

            List<String> backup = List.of(uploadedFile.getAbsolutePath());
            if (fileBuffer.getFileName().toLowerCase().contains(site.getName().toLowerCase())) {
                backupTools.RestoreSites(backup,true);
                String message = site.getName() + " checkin in";
                Notification.show(message);
                log.info(DB,message);
                upload.clearFileList();
                try {
                    Files.delete(uploadedFile.toPath());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                String message = site.getName() + " checkin failed invalid file";
                Notification.show(message);
                log.error(DB, message);
            }
            fireEvent(new CancelEvent(this.getSource()));
        }
    }


    public static class CancelEvent extends CheckoutFormEvent {
        CancelEvent(CheckinForm source) {
            super(source, null);
        }
    }

    public Registration addCheckoutListener(ComponentEventListener<CheckoutEvent> listener) {
        return addListener(CheckoutEvent.class, listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}
