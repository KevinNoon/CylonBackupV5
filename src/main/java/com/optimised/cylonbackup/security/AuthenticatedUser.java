package com.optimised.cylonbackup.security;

import com.optimised.cylonbackup.data.entity.User;
import com.optimised.cylonbackup.data.repository.UserRepo;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {
    final static Marker DB = MarkerManager.getMarker("DB");
    private static final Logger log = LogManager.getLogger(AuthenticatedUser.class);
    private final UserRepo userRepository;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepo userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        Optional<User> user = authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
        user.ifPresent(value -> log.info(DB, "User: {} logged in", value.getUsername()));
        return user;
    }

    public void logout() {
        if (authenticationContext.getPrincipalName().isPresent()) {
            log.info(DB, "User: {} logged out", authenticationContext.getPrincipalName().get());
        }
        authenticationContext.logout();
    }
}
