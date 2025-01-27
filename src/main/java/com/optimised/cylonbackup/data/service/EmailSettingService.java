package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.EmailSetting;
import com.optimised.cylonbackup.data.repository.EmailSettingRepo;
import com.vaadin.flow.component.notification.Notification;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EmailSettingService {

    private final EmailSettingRepo emailSettingRepo;
    final static Marker DB = MarkerManager.getMarker("DB");

    public EmailSettingService(EmailSettingRepo emailSettingRepo) {
        this.emailSettingRepo = emailSettingRepo;
    }

    public EmailSetting getSetting() {
        return emailSettingRepo.findById(1L).orElseGet(this::setDefault);
        }

    private EmailSetting setDefault(){
        EmailSetting emailSetting = new EmailSetting();
        emailSetting.setId(1L);
        emailSetting.setUserName("Someone@mail.com");
        emailSetting.setUserPassword("password");
        emailSetting.setPop3Host("pop.gmail.com");
        emailSetting.setPop3Port(995);
        emailSetting.setSmtpHost("smtp.gmail.com");
        emailSetting.setSmtpPort(587);
        emailSetting.setSmtpAuth(true);
        emailSetting.setSmtpStartTlsReq(true);
        emailSetting.setSmtpStarttlsEnable(true);
        emailSetting.setSmtpFrom("Someone@mail.com");
        emailSettingRepo.save(emailSetting);
        return emailSetting;
    }

    public void saveSetting(EmailSetting setting) {

        EmailSetting emDb = emailSettingRepo.findById(1L).get();
        if (!emDb.getSmtpHost().equals(setting.getSmtpHost())) {
            log.info(DB,"Email setting user name. From {} to {} ", emDb.getUserName(), setting.getUserName());
        }
        if (!emDb.getUserPassword().equals(setting.getUserPassword())) {
            log.info(DB,"Email setting user password updated.");
        }
        if (!emDb.getPop3Host().equals(setting.getPop3Host())) {
            log.info(DB, "Email setting Pop3 host updated. From {} to {} ", emDb.getPop3Host(), setting.getPop3Host());
        }
        if (!(emDb.getPop3Port() == (setting.getPop3Port()))) {
            log.info(DB, "Email setting Pop3 port updated. From {} to {} ", emDb.getPop3Port(), setting.getPop3Port());
        }
        if (!emDb.getSmtpHost().equals(setting.getSmtpHost())) {
            log.info(DB,"Email setting smtp host updated. From {} to {} ", emDb.getSmtpHost(), setting.getSmtpHost());
        }
        if (!(emDb.getSmtpPort() ==(setting.getSmtpPort()))) {
            log.info(DB,"Email setting smtp port updated. From {} to {} ", emDb.getSmtpPort(), setting.getSmtpPort());
        }
        if (!emDb.getSmtpFrom().equals(setting.getSmtpFrom())) {
            log.info(DB,"Email setting smtp from updated. From {} to {} ", emDb.getSmtpFrom(), setting.getSmtpFrom());
        }
        if (!(emDb.getSmtpAuth() ==(setting.getSmtpAuth()))) {
            log.info(DB,"Email setting smtp auth updated. From {} to {} ", emDb.getSmtpAuth(), setting.getSmtpAuth());
        }
        if (!(emDb.getSmtpStarttlsEnable() ==(setting.getSmtpStarttlsEnable()))) {
            log.info(DB,"Email setting smtp start tls enabled updated. From {} to {} ", emDb.getSmtpStarttlsEnable(), setting.getSmtpStarttlsEnable());
        }
        if (!(emDb.getSmtpStartTlsReq() ==(setting.getSmtpStartTlsReq()))) {
            log.info(DB,"Email setting smtp start tls required. From {} to {} ", emDb.getSmtpStartTlsReq(), setting.getSmtpStartTlsReq());
        }
        setting.setId(1L);
        emailSettingRepo.save(setting);
        Notification.show("Email, settings updated successfully");
        System.out.println(emDb);
        System.out.println(setting);

    }
}
