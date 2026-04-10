package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides standard CRUD operations and lookup methods by username.
 *
 * @see User
 * @see com.equipmentrental.equipment_rental_system.service.UserService
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return {@code true} if a user with this username exists
     */
    boolean existsByUsername(String username);
}
