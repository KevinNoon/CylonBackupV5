package com.optimised.cylonbackup.views.checkout;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.EmailService;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.optimised.cylonbackup.tools.BackupTools;
import com.optimised.cylonbackup.tools.FileTools;
import com.vaadin.flow.component.notification.Notification;
import lombok.Generated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class CheckinCommon {

    @Generated
    private static final Logger log = LogManager.getLogger(CheckinCommon.class);
    static final Marker DB = MarkerManager.getMarker("DB");

    public static void saveSite(Site site, SiteService siteService, BackupTools backupTools,
                                EmailService emailService, SettingService settingService, EngineerService engineerService, Boolean checkIn) {

        siteService.saveSite(site);

        String ccbFile = backupTools.Backup(site);
        boolean emailOk = emailService.sendMessageWithAttachment(site.getEngineer().getEmail(), "Cylon Backup",
            "Please rename from *.zip to *.ccb. This backup should be deleted fom you PC once you have returned it to the Bureau",
            ccbFile);
        if (emailOk) {
            Notification.show("Email sent site " + site.getName() +  " to engineer");
            log.error(DB,"Email to send site {} to engineer",site.getName());
            String sitePath = settingService.getSetting().getCylonPath() + "/" + site.getDirectory();
            FileTools.setFilesReadOnly(sitePath, false);
        } else {
            if (!checkIn) site.setEngineer(engineerService.findById(1L).get());

            Notification.show("Email failed to send site " + site.getName() +  " to engineer");
            log.error(DB,"Email failed to send site {} to engineer",site.getName());
        }
    }

}
