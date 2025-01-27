package com.optimised.cylonbackup.data.repository;

import com.optimised.cylonbackup.data.entity.EmailSetting;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmailSettingRepo extends JpaRepository<EmailSetting, Long> {
}
