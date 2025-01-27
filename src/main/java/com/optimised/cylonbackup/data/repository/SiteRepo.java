package com.optimised.cylonbackup.data.repository;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

public interface SiteRepo extends JpaRepository<Site,Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Site SET existing = false")
    void setExistingFalse();

    @Transactional
    @Modifying
    @Query("DELETE FROM Site WHERE existing=false ")
    void deleteByExistingFalse();

    // For Restoring site
    Site findFirstBySiteNumber(Integer siteNo);

    //For Site view - Visible if existing
    @Query("select s from Site s " +
        "where s.storeNumber = :sn " +
        " and s.existing = true")
    Optional<ArrayList<Site>> searchStoreNumberAndExisting(@Param("sn") Integer storeNo);

    @Query("select s from Site s " +
        "where lower(s.name) like lower(concat('%', :sn, '%')) " +
        " and s.existing = true")
    Optional<ArrayList<Site>> searchSiteNameAndExisting(@Param("sn") String searchTerm);


    //For Backup view - Visible if not checked out (Engineer ID = 1) AND existing
    @Query("select s from Site s " +
        "where s.storeNumber = :sn " +
        " and s.engineer.id = 1" +
        " and s.existing = true")
    Optional<ArrayList<Site>> searchStoreNumberAndNotCheckedOutAndExisting(@Param("sn") Integer storeNo);

    @Query("select s from Site s " +
        "where lower(s.name) like lower(concat('%', :sn, '%')) " +
        " and s.engineer.id = 1" +
        " and s.existing = true")
    Optional<ArrayList<Site>> searchSiteNameAndNotCheckedOutAndExisting(@Param("sn") String searchTerm);


    //For restore view - Visible if not checked out (Engineer ID = 1)
    @Query("select s from Site s " +
        "where s.storeNumber = :sn " +
        " and s.engineer.id = 1")
    Optional<ArrayList<Site>> searchStoreNumberAndNotCheckedOut(@Param("sn") Integer storeNo);

    @Query("select s from Site s " +
        "where lower(s.name) like lower(concat('%', :sn, '%')) " +
        " and s.engineer.id = 1")
    Optional<ArrayList<Site>> searchSiteNameAndNotCheckedOut(@Param("sn") String searchTerm);

    @Query("select s from Site s " +
        "where s.storeNumber = :sn" +
        "  and s.engineer.id != 1 and s.existing = true")
    Optional<ArrayList<Site>> searchStoreNumberAndCheckedOutAndExisting(@Param("sn") Integer storeNo);

    @Query("select s from Site s " +
        "where lower(s.name) like lower(concat('%', :sn, '%')) " +
        " and s.engineer.id != 1 and s.existing = true")
    Optional<ArrayList<Site>> searchSiteNameAndCheckedOutAndExisting(@Param("sn") String searchTerm);

    @Query("select s from Site s " +
        "where lower(s.name) like lower(concat('%', :sn, '%')) " +
        " and lower(s.directory) like lower(concat('%', :dr, '%'))")
    Optional<Site> searchSiteByNameAndDirectory(@Param("sn") String searchTerm, @Param("dr") String directory);

    @Query("select s from Site s where s.engineer = :en")
    Optional<Site> searchSiteByEngineer(@Param("en") Engineer engineer);
}
