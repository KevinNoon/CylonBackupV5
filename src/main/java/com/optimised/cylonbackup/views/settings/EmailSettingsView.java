package com.optimised.cylonbackup.views.settings;

import com.optimised.cylonbackup.data.entity.EmailSetting;
import com.optimised.cylonbackup.data.service.EmailSettingService;
import com.optimised.cylonbackup.views.CommonObjects;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.Generated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RolesAllowed({"ADMIN"})
@PageTitle("Email Settings")
@Route(value = "emailSettings", layout = MainLayout.class)
public class EmailSettingsView extends VerticalLayout {
    final EmailSettingService emailSettingService;
    @Generated
    private static final Logger log = LogManager.getLogger(EmailSettingsView.class);
    TextField userName = new TextField("Username");
    PasswordField userPassword = new PasswordField("Password");
    TextField pop3Host = new TextField("Pop3 Host");
    IntegerField pop3Port = new IntegerField("Pop3 Port");
    TextField smtpHost = new TextField("SMTP Host");
    IntegerField smtpPort = new IntegerField("SMTP Port");
    EmailField smtpFrom = new EmailField("From");
    Checkbox smtpAuth = new Checkbox("SMTP Auth");
    Checkbox smtpStarttlsEnable = new Checkbox("SMTP Starttls Enable");
    Checkbox smtpStartTlsReq = new Checkbox("SMTP Start TLS Req");
    Button save = new Button("Save");
    Button cancel = new Button("Cancel");

    Binder<EmailSetting> binder = new BeanValidationBinder<>(EmailSetting.class);

    public void setSettings(EmailSetting settings){
        binder.setBean(settings);
    }

    public EmailSettingsView(EmailSettingService emailSettingService) {
        binder.bindInstanceFields(this);
        this.emailSettingService = emailSettingService;
        setSettings(emailSettingService.getSetting());
        createLayout();
        updateValues();
        add(userName, userPassword,pop3Host,pop3Port, smtpHost, smtpPort,smtpFrom,smtpAuth,smtpStarttlsEnable,smtpStartTlsReq, createButtonsLayout());
    }

    private void   createLayout(){
        userName.setWidth("25%");
        userName.setPlaceholder("someone@mail.com");
        userName.setErrorMessage("Invalid username");
        userName.setRequired(true);
        userName.setRequiredIndicatorVisible(true);
        userName.setMinLength(3);
        userPassword.setWidth("25%");
        userPassword.setPlaceholder("Password");
        userPassword.setRequired(true);
        pop3Host.setWidth("25%");
        pop3Host.setPlaceholder("Host");
        pop3Host.setRequired(true);
        pop3Port.setWidth("25%");
        pop3Port.setPlaceholder("Port");
        smtpHost.setWidth("25%");
        smtpHost.setPlaceholder("Host");
        smtpHost.setRequired(true);
        smtpPort.setWidth("25%");
        smtpPort.setPlaceholder("Port");
        smtpFrom.setWidth("25%");
        smtpFrom.setPlaceholder("From");
        smtpAuth.setWidth("25%");
        //smtpAuth.setValue(true);
        smtpStarttlsEnable.setWidth("25%");
        //smtpStarttlsEnable.setValue(true);
        smtpStartTlsReq.setWidth("25%");
        //smtpStartTlsReq.setValue(true);
    }

    private void updateValues(){
        EmailSetting emailSetting = emailSettingService.getSetting();
        System.out.println(emailSetting);
        userName.setValue(emailSetting.getUserName());
        userPassword.setValue(emailSetting.getUserPassword());
        pop3Host.setValue(emailSetting.getPop3Host());
        pop3Port.setValue(emailSetting.getPop3Port());
        smtpHost.setValue(emailSetting.getSmtpHost());
        smtpPort.setValue(emailSetting.getSmtpPort());
        smtpFrom.setValue(emailSetting.getSmtpFrom());
        smtpAuth.setValue(emailSetting.getSmtpAuth());
        smtpStarttlsEnable.setValue(emailSetting.getSmtpStarttlsEnable());
        smtpStartTlsReq.setValue(emailSetting.getSmtpStartTlsReq());
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
         emailSettingService.saveSetting(binder.getBean());};
        }
}
