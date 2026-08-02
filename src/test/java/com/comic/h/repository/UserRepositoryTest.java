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
    @DisplayName("findByUserName - Trả về Optional<User> khi user có trong DB")
    void findByUserName_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setUserName("testrepo");
        user.setUserPassword("pass123");
        user.setUserRole(Role.USER);
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUserName("testrepo");

        assertTrue(found.isPresent());
        assertEquals("testrepo", found.get().getUserName());
    }

    @Test
    @DisplayName("findByUserName - Trả về Optional.empty() khi user không có trong DB")
    void findByUserName_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByUserName("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("existsByUserName - Trả về true khi username tồn tại")
    void existsByUserName_WhenUserExists_ShouldReturnTrue() {
        User user = new User();
        user.setUserName("existing");
        user.setUserPassword("pass123");
        user.setUserRole(Role.USER);
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByUserName("existing");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByUserName - Trả về false khi username chưa tồn tại")
    void existsByUserName_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUserName("notexist");

        assertFalse(exists);
    }
}
