package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table
public class EmailSetting {
    @Id
    private Long id;
    @NotEmpty
    @Size(min = 3, max = 24)
    private String userName;
    @NotEmpty
    @Size(min = 3, max = 24)
    private String userPassword;
    @NotEmpty
    @Size(min = 3, max = 24)
    private String pop3Host;
    @NotNull
    @Min(1)
    private int pop3Port;
    @NotEmpty
    @Size(min = 1, max = 24)
    private String smtpHost;
    @NotNull
    @Min(1)
    private int smtpPort;
    private String smtpFrom;
    private Boolean smtpAuth;
    private Boolean smtpStarttlsEnable;
    private Boolean smtpStartTlsReq;

}
