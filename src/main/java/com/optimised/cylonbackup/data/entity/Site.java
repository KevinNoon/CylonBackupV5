package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data

@Table
public class Site {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer siteNumber;
    private String name;
    private String directory;
    private Integer alarmScan;
    private String iDCode;
    private String telephone;
    private Integer remote;
    private Integer network;
    private LocalDateTime BackupTime;
    private Integer internet;
    private String ipAddr;
    private Integer port;
    private Integer bacNet;
    private Integer defaultType;
    private Boolean existing;
    private Integer storeNumber;
    private LocalDate expectedReturnDate;

    @ManyToOne
    @JoinColumn(name = "engineer_id")
    private Engineer engineer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Site site = (Site) o;

        return siteNumber.equals(site.siteNumber);
    }

    @Override
    public int hashCode() {
        return siteNumber.hashCode();
    }
}
