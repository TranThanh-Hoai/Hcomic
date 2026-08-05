package com.comic.h.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.comic.h.entity.User;
import com.comic.h.enums.Role;
import com.comic.h.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAccountIfNotFound("user", Role.USER, "123456");
        createAccountIfNotFound("translator", Role.TRANSLATOR, "123456");
    }

    private void createAccountIfNotFound(String username, Role role, String rawPassword) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setDisplayName(username);
            userRepository.save(user);
            log.info("Registered initial account: username={}, role={}", username, role);
        }
    }
}
