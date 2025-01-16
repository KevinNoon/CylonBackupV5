package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.Setting;
import com.optimised.cylonbackup.data.repository.SettingRepo;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Log4j2
@Service
public class SettingService {
    final private SettingRepo settingRepo;
    final static Marker DB = MarkerManager.getMarker("DB");

    public SettingService(SettingRepo settingRepo) {
        this.settingRepo = settingRepo;
    }

    public Setting getSetting() {
        return settingRepo.findById(1L).orElseGet(this::setDefault);
    }

    private Setting setDefault() {
        Setting setting = new Setting();
        setting.setId(1L);
        setting.setCylonPath("C:\\UnitronUC32");
        setting.setBackupPath("C:\\Backup");
        setting.setAutoBackup(true);
        setting.setBackupTime(LocalTime.of(4, 0));
        setting.setAutoUpdateSites(false);
        setting.setUpdateSitesTime(LocalTime.of(1, 0));
        return setting;
    }


    public void saveSetting(Setting setting) {
        if (settingRepo.findById(1L).isPresent()) {
            Setting settingDB = settingRepo.findById(1L).get();
            if (!settingDB.getCylonPath().equals(setting.getCylonPath())) {
                String message = "Settings:Cylon path updated changed from " + settingDB.getCylonPath() + " to " + setting.getCylonPath();
                log.info(DB, message);
            }
            if (!settingDB.getBackupPath().equals(setting.getBackupPath())) {
                String message = "Settings:Backup path updated changed from " + settingDB.getBackupPath() + " to " + setting.getBackupPath();
                log.info(DB, message);
            }
            if (!settingDB.getAutoBackup().equals(setting.getAutoBackup())) {
                String message = "Settings:Auto Backup changed from " + settingDB.getAutoBackup() + " to " + setting.getAutoBackup();
                log.info(DB, message);
            }
            if (!settingDB.getBackupTime().equals(setting.getBackupTime())) {
                String message = "Settings:Backup Time changed from " + settingDB.getBackupTime() + " to " + setting.getBackupTime();
                log.info(DB, message);
            }
            if (!settingDB.getAutoUpdateSites().equals(setting.getAutoUpdateSites())) {
                String message = "Settings:Auto Update Sites changed from " + settingDB.getAutoUpdateSites() + " to " + setting.getAutoUpdateSites();
                log.info(DB, message);
            }
            if (!settingDB.getUpdateSitesTime().equals(setting.getUpdateSitesTime())) {
                String message = "Settings:Auto Update Sites Time changed from " + settingDB.getUpdateSitesTime() + " to " + setting.getUpdateSitesTime();
                log.info(DB, message);
            }
            settingRepo.save(setting);
        }
    }
}
