package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Data
@Table
public class Setting {
  @Id
  private Long id;
  private String cylonPath;
  private String backupPath;
  private Boolean autoBackup;
  private LocalTime backupTime;
  private Boolean autoUpdateSites;
  private LocalTime updateSitesTime;
  private Boolean exBackups;
  private Boolean exDrawings;
  private Boolean inAlarmsDB;
  private Boolean inPasswords;
  private Boolean inReports;
  private Boolean inWN3000ini;

}
