package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table
public class EmailSetting {
    @Id
    private Long id;
    private String userName;
    private String userPassword;
    private String pop3Host;
    private int pop3Port;
    private String smtpHost;
    private int smtpPort;
    private String smtpFrom;
    private Boolean smtpAuth;
    private Boolean smtpStarttlsEnable;
    private Boolean smtpStartTlsReq;

}
