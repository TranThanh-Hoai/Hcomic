package com.comic.h.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.comic.h.entity.Role;
import com.comic.h.entity.User;

@DataJpaTest
@TestPropertySource(locations = "classpath:application.properties")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername - Trả về Optional<User> khi user có trong DB")
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setUsername("testrepo");
        user.setPassword("pass123");
        user.setRole(Role.USER);
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("testrepo");

        assertTrue(found.isPresent());
        assertEquals("testrepo", found.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername - Trả về Optional.empty() khi user không có trong DB")
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("existsByUsername - Trả về true khi username tồn tại")
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        User user = new User();
        user.setUsername("existing");
        user.setPassword("pass123");
        user.setRole(Role.USER);
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByUsername("existing");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByUsername - Trả về false khi username chưa tồn tại")
    void existsByUsername_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("notexist");

        assertFalse(exists);
    }
}
