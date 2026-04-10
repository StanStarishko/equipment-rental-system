package com.equipmentrental.equipment_rental_system.exception;

/**
 * Thrown when an entity cannot be deleted because it has dependent records.
 * Covers BR-06 (category with assigned equipment) and BR-07 (equipment with active bookings).
 *
 * @see com.equipmentrental.equipment_rental_system.service.CategoryService#deleteById
 * @see com.equipmentrental.equipment_rental_system.service.EquipmentService#deleteById
 */
public class DeletionBlockedException extends RuntimeException {

    /**
     * Creates a new exception explaining why deletion was blocked.
     *
     * @param entityName the name of the entity that cannot be deleted
     * @param reason     the reason deletion is not allowed
     */
    public DeletionBlockedException(String entityName, String reason) {
        super("Cannot delete " + entityName + ": " + reason);
    }
}
