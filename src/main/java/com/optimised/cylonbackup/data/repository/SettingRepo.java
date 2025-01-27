package com.optimised.cylonbackup.data.repository;

import com.optimised.cylonbackup.data.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SettingRepo extends JpaRepository<Setting,Long> {
}
