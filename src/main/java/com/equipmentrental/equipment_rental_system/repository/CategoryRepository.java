package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 * Provides standard CRUD operations and lookup methods by name.
 *
 * @see Category
 * @see com.equipmentrental.equipment_rental_system.service.CategoryService
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its unique name.
     *
     * @param name the category name to search for
     * @return an {@link Optional} containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Checks whether a category with the given name already exists.
     *
     * @param name the category name to check
     * @return {@code true} if a category with this name exists
     */
    boolean existsByName(String name);
}
