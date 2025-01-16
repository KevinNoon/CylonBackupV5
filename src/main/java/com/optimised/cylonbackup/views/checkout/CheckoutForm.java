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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.optimised.cylonbackup.views.checkout.CheckinCommon.saveSite;

@Service
@Log4j2
public class CheckoutForm extends FormLayout {

    final EmailService emailService;
    final EngineerService engineerService;
    final SiteService siteService;
    final BackupTools backupTools;
    final SettingService settingService;
    final static Marker DB = MarkerManager.getMarker("DB");

    public void setSite(Site site) {
        binder.setBean(site);
    }

    //todo add code to reset read only files (via manager login)
    IntegerField storeNumber = new IntegerField("Store Number");
    TextField name = new TextField("Name");
    IntegerField siteNumber = new IntegerField("Site Number");
    ComboBox<Engineer> engineer = new ComboBox<>("Engineers");
    DatePicker expectedReturnDate = new DatePicker("Expected Return Date");
    Button checkout = new Button("Checkout");
    Button cancel = new Button("Cancel");
    Binder<Site> binder = new BeanValidationBinder<>(Site.class);

    public CheckoutForm(EmailService emailService, EngineerService engineerService, SiteService siteService, BackupTools backupTools, SettingService settingService) {
        this.emailService = emailService;
        this.engineerService = engineerService;
        this.siteService = siteService;
        this.backupTools = backupTools;
        this.settingService = settingService;

        addClassName("site-form");
        binder.bindInstanceFields(this);
        storeNumber.setReadOnly(true);
        name.setReadOnly(true);
        engineer.setItemLabelGenerator(Engineer::getFullName);
        engineer.setItems(this.engineerService.findAllEngineers());

   //     engineer.setPlaceholder("Select Engineer");
        siteNumber.setReadOnly(true);
        expectedReturnDate.setMin(LocalDate.now().plusDays(1L));
        expectedReturnDate.setMax(LocalDate.now().plusDays(14L));
        expectedReturnDate.setValue(LocalDate.now().plusDays(1L));

        add(
            storeNumber,
            name,
            siteNumber,
            engineer,
            expectedReturnDate,
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

        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickShortcut(Key.ESCAPE);
        cancel.addClickListener(event -> fireEvent(new CancelEvent(this)));
        binder.addStatusChangeListener(e -> checkout.setEnabled(binder.isValid()));
        return new HorizontalLayout(checkout, cancel);
    }

    @Getter
    public static abstract class CheckoutFormEvent extends ComponentEvent<CheckoutForm> {
        private final Site site;
        protected CheckoutFormEvent(CheckoutForm source, Site site) {
            super(source, false);
            this.site = site;
        }
    }

    public class CheckoutEvent extends CheckoutFormEvent {
        CheckoutEvent(CheckoutForm source, Site site) {
            super(source, site);
            if (site.getEngineer().getId() != 1) {
                if (expectedReturnDate.getValue() == null) {
                    expectedReturnDate.setValue(LocalDate.now().plusDays(1L));
                }
                site.setExpectedReturnDate(expectedReturnDate.getValue());
                saveSite(site, siteService, backupTools, emailService, settingService, engineerService,false);
            } else {
                Notification.show("No engineer selected");
            }
        }
    }

    public static class CancelEvent extends CheckoutFormEvent {
        CancelEvent(CheckoutForm source) {
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

