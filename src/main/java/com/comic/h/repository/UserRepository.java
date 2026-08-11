package com.comic.h.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.User;
import com.comic.h.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    long countByIsBannedTrue();

    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isBanned IS NULL OR u.isBanned = :isBanned)")
    Page<User> searchUsers(@Param("query") String query,
                           @Param("role") Role role,
                           @Param("isBanned") Boolean isBanned,
                           Pageable pageable);
}
