package com.equipmentrental.equipment_rental_system.exception;

import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;

public class EquipmentNotAvailableException extends RuntimeException {

    public EquipmentNotAvailableException(String equipmentName, EquipmentStatus currentStatus) {
        super("Equipment '" + equipmentName + "' is not available for booking. "
                + "Current status: " + currentStatus);
    }
}
