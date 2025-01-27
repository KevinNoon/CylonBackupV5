package com.optimised.cylonbackup.data.repository;


import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);
    User findById(long id);

    @Query("select u from User u " +
        "where lower(u.name) = lower(:un) " +
        " and lower(u.username) = lower(:uun)")
    Optional<User> findByNameAndUserName(@Param("un") String name, @Param("uun") String userName);
}
