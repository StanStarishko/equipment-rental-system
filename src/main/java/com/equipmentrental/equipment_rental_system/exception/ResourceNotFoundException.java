package com.equipmentrental.equipment_rental_system.exception;

/**
 * Thrown when a requested entity cannot be found by its ID.
 * This is a generic exception used across all service classes when
 * a lookup by primary key returns no result.
 *
 * @see com.equipmentrental.equipment_rental_system.controller.GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param resourceName the type of resource (e.g. "Equipment", "Booking")
     * @param resourceId   the ID that was not found
     */
    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(resourceName + " with ID " + resourceId + " was not found");
    }
}
