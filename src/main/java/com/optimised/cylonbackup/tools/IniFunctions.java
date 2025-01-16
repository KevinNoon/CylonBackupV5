package com.optimised.cylonbackup.tools;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.SettingService;
import com.optimised.cylonbackup.data.service.SiteService;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class IniFunctions {

    final SiteService siteService;
    final SettingService settingService;
    public static boolean update;
    final static Marker DB = MarkerManager.getMarker("DB");

    public IniFunctions(SiteService siteService, SettingService settingService) {
        this.siteService = siteService;

        this.settingService = settingService;
    }

    String READ_INI_ERROR = "Unable to read Cylon file because of ";

    public class updateTask implements Runnable {
        @Override
        public void run() {
            SetSiteInfo();
        }
    }

    public void ManualUpdate(){
        updateTask task = new updateTask();
        task.run();
        update = false;
    }

    @Scheduled(fixedRate = 60_000L)
    public void UpdateSitesTask(){
        LocalTime st = settingService.getSetting().getUpdateSitesTime();
        LocalTime tn = LocalTime.now();
        if ((settingService.getSetting().getAutoUpdateSites()
            && st.getHour() == tn.getHour() && st.getMinute() == tn.getMinute())) {
            updateTask task = new updateTask();
            task.run();
        }
    }

    public void SetSiteInfo() {
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            int noOfSites = ini.get("SiteList", "TotalSites", int.class);
            siteService.setExistingFalse();
            for (int n = 1; n < noOfSites + 1; n++) {

                Integer siteNo = Conversions.tryParseInt(ini.get("SiteList", "Site" + n));
                siteService.saveSite(GetSiteFromSiteNo(siteNo));
            }
            log.info(DB,"Site database has been updated");
        } catch (IOException e) {
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
    }

    public Site GetSiteFromSiteNo(Integer siteNo){
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        Site site = new Site();
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            String siteSection = "Site" + siteNo;
            site.setSiteNumber(siteNo);
            site.setDirectory(ini.get(siteSection, "Directory"));
            site.setAlarmScan(Conversions.tryParseInt(ini.get(siteSection, "AlarmScan")));
            site.setInternet(Conversions.tryParseInt(ini.get(siteSection, "Internet")));
            site.setIDCode(ini.get(siteSection, "IDCode"));
            site.setIpAddr(ini.get(siteSection, "IPAddr"));
            site.setName(ini.get(siteSection, "Name"));
            site.setNetwork(Conversions.tryParseInt(ini.get(siteSection, "Network")));
            site.setPort(Conversions.tryParseInt(ini.get(siteSection, "Port")));
            site.setRemote(Conversions.tryParseInt(ini.get(siteSection, "Remote")));
            site.setTelephone(ini.get(siteSection, "Telephone"));
            site.setBacNet(Conversions.tryParseInt(ini.get(siteSection, "BacNet")));
            site.setDefaultType(Conversions.tryParseInt(ini.get(siteSection, "DefaultType")));
            site.setExisting(true);
            Site siteOrg = siteService.findSiteBySiteNo(siteNo);
            Engineer engineer = new Engineer();
            if (siteOrg != null) {
                engineer.setId(siteOrg.getEngineer().getId());
            } else {
                engineer.setId(1L);
            }
            site.setEngineer(engineer);
        }catch (IOException e){
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
        return site;
    }

    public void setSiteDetails(Site site){
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        //Check in site is a new site
        if (!CheckSiteNoExists(site.getSiteNumber())) {
            try {
                Wini ini = new Wini(new File(wn3000IniPath));
                Profile.Section siteListSection = ini.get("SiteList");
                int siteId = Integer.parseInt(siteListSection.get("TotalSites"));
                siteListSection.put("Site" + (siteId + 1), site.getSiteNumber());
                siteListSection.remove("TotalSites");
                siteListSection.put("TotalSites", (siteId + 1));
                //Get a site as it is not possible to create a new section
                Profile.Section newSite = ini.add("Site" + siteListSection.get("Site" + (siteId + 1)));
                int as = site.getAlarmScan() == null?1:site.getAlarmScan();
                newSite.put("AlarmScan",as);
                newSite.put("BACnet",site.getBacNet().toString());
                newSite.put("DefaultType",site.getDefaultType().toString());
                newSite.put("Directory",site.getDirectory());
                newSite.put("Internet",site.getInternet().toString());
                newSite.put("IPAddr",site.getIpAddr());
                newSite.put("Name",site.getName());
                newSite.put("Network",site.getNetwork().toString());
                newSite.put("Port",site.getPort().toString());
                newSite.put("Remote",site.getRemote().toString());
                newSite.put("Telephone",site.getTelephone());
                newSite.put("BackupTime",site.getBackupTime().toString());
                ini.store();
            } catch (IOException e) {
                log.error("Failed to open WN3000.ini file {}", e.getMessage());
            }
        }

        int siteNo = site.getSiteNumber();
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            Profile.Section siteSection = ini.get("Site" + siteNo);
            if (siteSection != null) {

                if (site.getAlarmScan() != null) siteSection.put("AlarmScan",site.getAlarmScan().toString());
                if (site.getBacNet() != null) siteSection.put("BACnet",site.getBacNet().toString());
                if (site.getDefaultType() != null) siteSection.put("DefaultType",site.getDefaultType().toString());
                if (site.getDirectory() != null) siteSection.put("Directory",site.getDirectory());
                if (site.getInternet() != null) siteSection.put("Internet",site.getInternet().toString());
                if (site.getIpAddr() != null) siteSection.put("IPAddr",site.getIpAddr());
                if (site.getName() != null) siteSection.put("Name",site.getName());
                if (site.getNetwork() != null) siteSection.put("Network",site.getNetwork().toString());
                if (site.getPort() != null) siteSection.put("Port",site.getPort().toString());
                if (site.getRemote() != null) siteSection.put("Remote",site.getRemote().toString());
                if (site.getTelephone() != null)siteSection.put("Telephone",site.getTelephone());
                if (site.getBackupTime() != null) siteSection.put("BackupTime",site.getBackupTime().toString());
                ini.store();
            } else {
                log.error(DB,"Site not found");
            }



        }catch (IOException e){
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
    }

    /**
    *<li>-1 Invalid name and directory (name has another directory in the database or vise versa)</li>
     *<li> 0 Directory and name not found (New Site) </li>
     *<li> > 0 Site number of the directory and name </li>
     */
    public Integer GetSiteNoFromSiteNameAndDir(String siteName, String directory) {
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            Profile.Section sites = ini.get("SiteList");
            for (Map.Entry<String, String> entry : sites.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("TotalSites")) {
                    Profile.Section site = ini.get("Site" + entry.getValue());
                    if (site == null) {return -1;}
                    if (site.get("Name").equalsIgnoreCase(siteName)
                        && site.get("Directory").equalsIgnoreCase(directory)) {
                        return Integer.parseInt(entry.getValue());
                    } else if ((site.get("Name").equalsIgnoreCase(siteName)
                        || (site.get("Directory").equalsIgnoreCase(directory)))) {
                        return -1;
                    }
                }
            }
        } catch (IOException e) {
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
        return 0;
    }

    public Boolean CheckSiteNoExists(Integer siteNo){
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            Profile.Section sites = ini.get("SiteList");
            if (sites.containsValue(siteNo.toString())) {
                return true;
            }
        }catch (IOException e) {
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
        return false;
    }

    public Integer getNextFreeSiteNo() {
        Integer nextFreeNo = 1;
        String wn3000IniPath = settingService.getSetting().getCylonPath() + "/system/wn3000.ini";
        try {
            Wini ini = new Wini(new File(wn3000IniPath));
            Profile.Section sites = ini.get("SiteList");
            List<Integer> siteNos = new ArrayList<Integer>();
            for (Map.Entry<String, String> entry : sites.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("TotalSites")) {
                    siteNos.add(Integer.parseInt(entry.getValue()));
                }
            }
            siteNos.sort(Integer::compareTo);
            for (Integer siteNo : siteNos) {
                if (!nextFreeNo.equals(siteNo)) {
                    nextFreeNo = nextFreeNo++;
                    break;
                }
                nextFreeNo++;
            }
        } catch (IOException e) {
            log.error(DB,"{}{}", READ_INI_ERROR, e.getMessage());
        }
        return nextFreeNo;
    }
}
