package com.optimised.cylonbackup.tools;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.EmailService;
import com.optimised.cylonbackup.data.service.EngineerService;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import com.vaadin.flow.component.notification.Notification;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Log4j2
@Component
public class BackupTools {
    final SiteService siteService;
    final SettingService settingService;
    final IniFunctions iniFunctions;
    final static Marker DB = MarkerManager.getMarker("DB");
    private final EngineerService engineerService;
    private final EmailService emailService;

    public BackupTools(SiteService siteService, SettingService settingService, IniFunctions iniFunctions, EngineerService engineerService, EmailService emailService) {
        this.siteService = siteService;
        this.settingService = settingService;
        this.iniFunctions = iniFunctions;
        this.engineerService = engineerService;
        this.emailService = emailService;
    }

    public String Backup(Site site) {

        if (!(site.getName() == null || site.getSiteNumber() == null)) {

            Path cylonPath = Path.of(settingService.getSetting().getCylonPath());
            Path backupPath = Path.of(settingService.getSetting().getBackupPath());
            Path backupOldPath = Path.of(backupPath + "\\old");
            Path workingPath = Path.of("Working");
            Path sourceDir = Path.of(cylonPath.toAbsolutePath() + "\\" + site.getDirectory());
            Path ucUnitronUC32Dir = Path.of(workingPath.toAbsolutePath() + "\\UnitronUC32\\");
            Path workingDir = Path.of(workingPath.toAbsolutePath() + "\\UnitronUC32\\" + site.getDirectory());
            
            String zipFileName;
            LocalDateTime backupTime = LocalDateTime.now();
            try {

                //Clean working directory before backup.
                if (Files.exists(ucUnitronUC32Dir)) {
                    FileTools.setFilesReadOnly(ucUnitronUC32Dir.toAbsolutePath().toString(),true);
                    PathUtils.deleteDirectory(ucUnitronUC32Dir.toAbsolutePath());
                }
                Files.createDirectories(workingDir);
                FileTools.setFilesReadOnly(sourceDir.toAbsolutePath().toString(),true);
                List<String> extensions = new ArrayList<>();
                if (settingService.getSetting().getExBackups()){
                    extensions.add(" backup");
                    extensions.add(".bak");
                }
                if (settingService.getSetting().getExDrawings()){
                    extensions.add(".drw");
                }
                FileTools.copyDirectories(sourceDir.toFile(), workingDir.toFile(),extensions,true);
                Files.createDirectories(backupPath);
                Files.createDirectories(backupOldPath);

            } catch (IOException e) {
                log.error(DB, "Failed to backup site due to {}",e.getMessage());
            }

            try {

                FileWriter fw = new FileWriter(workingDir + "\\CCBackUp.TXT");
                fw.append(site.getName()).append("\n");
                fw.append(site.getDirectory()).append("\n");
                if (site.getIDCode() != null) {
                    fw.append(site.getIDCode()).append("\n");
                } else {
                    fw.append("Error\n");
                }
                if (site.getTelephone() != null) {
                    fw.append(site.getTelephone()).append("\n");
                } else {
                    fw.append("Error\n");
                }
                fw.append(String.valueOf(site.getRemote())).append("\n");
                fw.append(String.valueOf(site.getNetwork())).append("\n");
                fw.append(backupTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
                fw.append(String.valueOf(site.getInternet())).append("\n");
                if (site.getIpAddr() != null) {
                    fw.append(site.getIpAddr()).append("\n");
                } else {
                    fw.append("Error\n");
                }
                fw.append(String.valueOf(site.getPort())).append("\n");
                fw.append(String.valueOf(site.getBacNet())).append("\n");
                fw.append(String.valueOf(site.getDefaultType())).append("\n");
                fw.close();

            } catch (IOException e) {
                log.error(DB, "Failed to backup site due to %s".formatted(e.getMessage()));
            }
            try {
                FileWriter fw = new FileWriter(workingDir + "\\SiteID.TXT");
                fw.append(String.valueOf(site.getSiteNumber())).append("\n");
                fw.close();
            } catch (IOException e) {
                log.error(DB, "Failed to backup site due to {}",e.getMessage());
            }
            try {
                FileWriter fw = new FileWriter(workingDir + "\\SiteName.TXT");
                fw.append(site.getName()).append("\n");
                fw.close();
            } catch (IOException e) {
                log.error(DB, "Failed to backup site due to %s".formatted(e.getMessage()));
            }
            zipFileName = FileTools.zipDirectory(workingDir, site.getName(), backupPath, backupOldPath);
            //Update the last backup time in the database.
            site.setBackupTime(backupTime);
            siteService.saveSite(site);

            List<String> extensions = new ArrayList<>();
            if (settingService.getSetting().getInAlarmsDB()) {
                extensions.add("alarm.mdb");
                extensions.add("alarm configuration.mdb");
            }
            if (settingService.getSetting().getInReports()) {
                extensions.add("1.ini");
            }
            if (settingService.getSetting().getInPasswords()){
                extensions.add("cylon.ini");
            }
            if (settingService.getSetting().getInWN3000ini()){
                extensions.add("wn3000.ini");
            }
            if (!extensions.isEmpty()){
                FileTools.copyDirectories(new File(cylonPath.toAbsolutePath().toString() + "\\system") , new File(backupPath.toAbsolutePath().toString() + "\\system\\" + backupTime.toLocalDate().toString()),extensions,false);
            }

            log.info(DB,"%s (no %s) was backed up".formatted(site.getName(), site.getSiteNumber()));
            Notification notification = new Notification("%s (site no %s) was backed up".formatted(site.getName(), site.getSiteNumber()));
            notification.setDuration(5000);
            notification.open();
            return zipFileName;
        } else {
            log.error(DB, "Back site failed");
            return null;
        }
    }

    @Scheduled(cron = "0 0/1 * * * *") //Run every minute
    public void AutoBackup() {
        Boolean updateSites = settingService.getSetting().getAutoBackup();
        if (updateSites) {
            LocalTime getDataTime = settingService.getSetting().getBackupTime();
            LocalTime currentTime = LocalTime.now();
            boolean timesMatched = (getDataTime.getHour() == currentTime.getHour()) && (getDataTime.getMinute() == currentTime.getMinute());

            if (timesMatched) {
                String cylonPath = settingService.getSetting().getCylonPath();
                List<Site> sites = new ArrayList<>();
                if (siteService.searchSiteNameAndNotCheckedOutAndExisting("").isPresent()) {
                    sites = siteService.searchSiteNameAndNotCheckedOutAndExisting("").get();
                }

                sites.forEach(site -> {
                    //We need to get the folder in the directory and then read the timestamp of the files in the directories.
                    File file = new File(cylonPath + "\\" + site.getDirectory() + "\\STRAT5");
                    if (file.exists()) {
                        List<File> dirs = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(File::isDirectory).toList();
                        //Now get the files in the directories
                        if (!(site.getBackupTime() == null)) {
                            for (File dirName : dirs) {
                                //Find the file with the latest update
                                Optional<File> fileNewest = Arrays.stream(Objects.requireNonNull(dirName.listFiles())).filter(file1 -> file1.isFile() && file1.getName().toLowerCase().contains("etg")).max(Comparator.comparingLong(File::lastModified));
                                LocalDateTime dt;
                                dt = fileNewest.isPresent() ? Instant.ofEpochMilli(fileNewest.get().lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.MIN;
                                if (site.getBackupTime().isBefore(dt)) {
                                    Backup(site);
                                    break;
                                }
                            }
                        } else {
                            Backup(site);
                        }
                    }
                });
            }
        }
    }

    public void RestoreSite(String backupPath, Site site) {
        iniFunctions.setSiteDetails(site);
        File cylonPath = new File(settingService.getSetting().getCylonPath()).getParentFile();
        FileTools.unZipBackup(backupPath, cylonPath.getAbsolutePath());
        String email = "";
        if (site.getEngineer().getId() > 1) email = site.getEngineer().getEmail();
        Optional<Engineer> engineer = engineerService.findById(1L);
        engineer.ifPresent(site::setEngineer);
        siteService.saveSite(site);

        String sitePath = cylonPath.getAbsolutePath() + "/UnitronUC32/" + site.getDirectory();
        FileTools.setFilesReadOnly(sitePath,true);
        if (!email.isEmpty()){
           if(emailService.sendSimpleMessage(email,"Site restored",site.getName() + " has been restored")){
               log.info("Email sent to {} that site {} has been restored",email,site.getName());
           } else {
               log.error("Failed to send email to {} that site {}",email,site.getName());
           }
        }
    }

    public void RestoreSites(List<String> backups,Boolean isCheckIn) {

        backups.forEach(b -> {
            Site siteBackup = FileTools.getSiteFromBackup(Path.of(b));
            if (siteBackup.getName() != null) {
                Integer nameAndDirMatch = iniFunctions.GetSiteNoFromSiteNameAndDir(siteBackup.getName(), siteBackup.getDirectory());

//                System.out.println("SBN " + siteBackup.getName() + " SBD " + siteBackup.getDirectory());
//                System.out.println("Site No " + nameAndDirMatch + " " + siteBackup.getName());

                boolean checkedOut = false;
                if (siteService.searchSiteNameAndCheckedOutAndExisting(siteBackup.getName()).isPresent()) {
                    if(!siteService.searchSiteNameAndCheckedOutAndExisting(siteBackup.getName()).get().isEmpty()) checkedOut = true;
                }

                if (checkedOut && !isCheckIn){
                    Notification notification = new Notification("site is checked out. Please check in site");
                    notification.setDuration(5000);
                    notification.open();
                    log.error(DB, "{}{}{}","Unable to restore ",siteBackup.getName()," site is checked out. Please check in site");
                } else if (nameAndDirMatch == -1) {
                    log.error(DB, "{}{}{}","Unable to restore ",siteBackup.getName()," due to error in backup");
                } else {
                    if (nameAndDirMatch == 0) {
                        if (iniFunctions.CheckSiteNoExists(siteBackup.getSiteNumber())) {
                            int siteNo = iniFunctions.getNextFreeSiteNo();
                            siteBackup.setSiteNumber(siteNo);
                            RestoreSite(b, siteBackup);
                            Backup(siteBackup);
                            siteService.saveSite(siteBackup);
                        }
                        RestoreSite(b, siteBackup);
                    } else  {
                        siteBackup.setSiteNumber(nameAndDirMatch);
                        Site site = iniFunctions.GetSiteFromSiteNo(siteBackup.getSiteNumber());
                        if (site.getAlarmScan() != null) {
                            siteBackup.setAlarmScan(site.getAlarmScan());
                        }
                        Backup(siteBackup);
                        RestoreSite(b, siteBackup);
                        Backup(siteBackup);
                        siteService.saveSite(siteBackup);
                    }
                }
            }
            log.info(DB, "Sites backup completed");
        });
    }

        @Scheduled(cron = "0 10 1 * * *")
        private void sendReminder()
        {
            Optional<ArrayList<Site>> sites = siteService.searchSiteNameAndCheckedOutAndExisting("");
            if (sites.isPresent()){
                for (Site site : sites.get()) {
                    if (site.getExpectedReturnDate() != null && site.getExpectedReturnDate().isBefore(LocalDate.now())){
                        long daysOverDue = (ChronoUnit.DAYS.between(site.getExpectedReturnDate(), LocalDate.now()));
                        if ((daysOverDue + 1) % 2 == 0) {
                            if (emailService.sendSimpleMessage(site.getEngineer().getEmail(), "Cylon Backup",
                                "Please return the backup of " + site.getName() + " to the Bureau it is " + daysOverDue + " days over due")){
                                log.info("Email reminder sent to {}  for site {}", site.getEngineer().getEmail(), site.getName());
                            } else{
                                log.error("Failed to reminder sent to {}  for site {}", site.getEngineer().getEmail(), site.getName());
                            }

                        }
                    }
                }
            }
        }

}

