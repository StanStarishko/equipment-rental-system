package com.equipmentrental.equipment_rental_system.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(resourceName + " with ID " + resourceId + " was not found");
    }
}
