package com.optimised.cylonbackup.data.repository;

import com.optimised.cylonbackup.data.entity.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface EngineerRepo extends JpaRepository<Engineer,Long> {
  @Query("select e from Engineer e " +
      "where e.id > 1")

  List<Engineer> findAll();


}
