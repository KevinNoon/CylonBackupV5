package com.optimised.cylonbackup.data.service;

import com.optimised.cylonbackup.data.entity.User;
import com.optimised.cylonbackup.data.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo repository) {
        this.userRepo = repository;
    }

    public Optional<User> get(Long id) {
        return userRepo.findById(id);
    }

    public User update(User entity) {
        return userRepo.save(entity);
    }

    public void delete(Long id) {
        if (userRepo.count() > 1) {
            userRepo.deleteById(id);
        }
    }

    public Page<User> list(Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return userRepo.findAll(filter, pageable);
    }

    public int count() {
        return (int) userRepo.count();
    }

    public void save(User user){
        userRepo.save(user);
    }

    public User findUserByUserName(String username){
        return userRepo.findByUsername(username);
    }

    public User findUserByNameAndUserName(String name,String username){
        Optional <User> user = userRepo.findByNameAndUserName(name,username);
        return user.orElseGet(User::new);
    }

    public Optional<User> findUserById(Long id){
            return userRepo.findById(id);
    }

    public List<User> findAllUsers(){
        return userRepo.findAll();
    }

}
