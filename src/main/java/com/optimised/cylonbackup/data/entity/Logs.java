package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Logs {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "log_date")
  private LocalDateTime logDate;
  private String logger;
  private String level;
  private String message;
  private String USER_ID;
}
