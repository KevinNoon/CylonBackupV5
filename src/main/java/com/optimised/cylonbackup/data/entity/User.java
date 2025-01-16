package com.optimised.cylonbackup.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optimised.cylonbackup.data.Role;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "application_user")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    private Boolean isdarkmode;
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Boolean getIsdarkmode() {
        return isdarkmode;
    }

    public void setIsdarkmode(Boolean isdarkmode) {
        this.isdarkmode = isdarkmode;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
