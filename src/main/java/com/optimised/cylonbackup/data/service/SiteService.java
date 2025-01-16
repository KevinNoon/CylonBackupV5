package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.repository.SiteRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SiteService {

    final private SiteRepo siteRepo;
    final private EngineerService engineerService;

    public SiteService(SiteRepo siteRepo, EngineerService engineerService) {
        this.siteRepo = siteRepo;
        this.engineerService = engineerService;
    }

    public void saveSite(Site site){
        Optional<Site> siteDB = siteRepo.searchSiteByNameAndDirectory(site.getName(),site.getDirectory());
        if (siteDB.isPresent()) {
            site.setId(siteDB.get().getId());
            if (site.getStoreNumber() == null){
                site.setStoreNumber((siteDB.get().getStoreNumber()));
            }
            if (site.getBackupTime() == null){
                site.setBackupTime(siteDB.get().getBackupTime());
            }
            if (site.getEngineer() == null){
                site.setEngineer(siteDB.get().getEngineer());
            }
        } else {
            if (site.getEngineer() == null){
                if (engineerService.findById(1L).isPresent()) {
                    site.setEngineer(engineerService.findById(1L).get());
                }
            }
        }
        siteRepo.save(site);
    }
    //todo Backup ini file
    public void setExistingFalse(){
        siteRepo.setExistingFalse();
    }

    public void deleteIfExisingFalse(){
        siteRepo.deleteByExistingFalse();
    }

    public Site findSiteBySiteNo(Integer siteNo){
        return siteRepo.findFirstBySiteNumber(siteNo);
    }


    //For Site view - Visible if existing
    public Optional<ArrayList<Site>> searchStoreNumberAndExisting(Integer storeNo)
    {return siteRepo.searchStoreNumberAndExisting(storeNo);}

    public Optional<ArrayList<Site>> searchSiteNameAndExisting(String storeName)
    {return siteRepo.searchSiteNameAndExisting(storeName);}


    //For Backup view - Visible if not checked out (Engineer ID = 1) AND existing
    public Optional<ArrayList<Site>> searchStoreNumberAndNotCheckedOutAndExisting(Integer storeNo)
    {return siteRepo.searchStoreNumberAndNotCheckedOutAndExisting(storeNo);}

    public Optional<ArrayList<Site>> searchSiteNameAndNotCheckedOutAndExisting(String storeName)
    {return siteRepo.searchSiteNameAndNotCheckedOutAndExisting(storeName);}


    //For restore view - Visible if not checked out (Engineer ID > 1)
    public Optional<ArrayList<Site>> searchStoreNumberAndNotCheckedOut(Integer storeNo)
    {return siteRepo.searchStoreNumberAndNotCheckedOut(storeNo);}

    public Optional<ArrayList<Site>> searchSiteNameAndNotCheckedOut(String storeName)
    {
        return siteRepo.searchSiteNameAndNotCheckedOut(storeName);}



    public Optional<ArrayList<Site>> searchStoreNumberAndCheckedOutAndExisting(Integer storeNo) {
        return this.siteRepo.searchStoreNumberAndCheckedOutAndExisting(storeNo);
    }

    public Optional<ArrayList<Site>> searchSiteNameAndCheckedOutAndExisting(String storeName) {
        return this.siteRepo.searchSiteNameAndCheckedOutAndExisting(storeName);
    }

    public Optional<Site> searchSiteNameAndDirectory(String name, String directory) {
        return this.siteRepo.searchSiteByNameAndDirectory(name, directory);
    }

    public Optional<Site> searchSiteByEngineer(Engineer engineer) {
        return this.siteRepo.searchSiteByEngineer(engineer);
    }
}
