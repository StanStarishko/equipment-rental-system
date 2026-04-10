package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.Equipment;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Equipment} entities.
 * Provides standard CRUD operations and derived query methods for
 * filtering equipment by category, status and name.
 *
 * @see Equipment
 * @see com.equipmentrental.equipment_rental_system.service.EquipmentService
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Finds all equipment items belonging to a specific category.
     *
     * @param categoryId the ID of the category
     * @return list of equipment in the given category
     */
    List<Equipment> findByCategoryId(Long categoryId);

    /**
     * Finds all equipment items with a specific availability status.
     *
     * @param status the equipment status to filter by
     * @return list of equipment matching the given status
     */
    List<Equipment> findByStatus(EquipmentStatus status);

    /**
     * Searches for equipment items whose name contains the given text (case-insensitive).
     *
     * @param name the search text to match against equipment names
     * @return list of equipment whose name contains the search text
     */
    List<Equipment> findByNameContainingIgnoreCase(String name);
}
