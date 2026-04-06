package com.equipmentrental.equipment_rental_system.exception;

public class DeletionBlockedException extends RuntimeException {

    public DeletionBlockedException(String entityName, String reason) {
        super("Cannot delete " + entityName + ": " + reason);
    }
}
