package com.optimised.cylonbackup.data.repository;

import com.optimised.cylonbackup.data.entity.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface EngineerRepo extends JpaRepository<Engineer,Long> {
  @Query("select e from Engineer e " +
      "where e.id > 1")

  List<Engineer> findAll();

  //Optional<Engineer> findByForenameAndLastname(String foreName, String lastname);

  @Query("select e from Engineer e " +
      "where lower(e.forename) = lower(:ef) " +
      " and lower(e.lastname) = lower(:el)")
  Optional<Engineer> findByForenameAndLastname(@Param("ef") String foreName, @Param("el") String lastname);
}
