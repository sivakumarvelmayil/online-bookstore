
package com.example.bookstore.repository;

import com.example.bookstore.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds a user by email during login.
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Checks whether an email is already registered.
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by ID and email.
     * Useful when validating user ownership.
     */
    Optional<AppUser> findByIdAndEmail(Long id, String email);

    /**
     * Checks whether a user exists with the given ID and email.
     */
    boolean existsByIdAndEmail(Long id, String email);
}
