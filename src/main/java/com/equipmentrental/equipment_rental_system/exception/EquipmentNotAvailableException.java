package com.equipmentrental.equipment_rental_system.exception;

import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;

/**
 * Thrown when a booking is attempted on equipment that is not available (BR-01).
 * Equipment must be in AVAILABLE or BOOKED status to accept new bookings;
 * items in MAINTENANCE or UNAVAILABLE status are rejected.
 *
 * @see com.equipmentrental.equipment_rental_system.service.EquipmentService#verifyAvailableForBooking
 */
public class EquipmentNotAvailableException extends RuntimeException {

    /**
     * Creates a new exception describing the unavailable equipment.
     *
     * @param equipmentName the name of the equipment item
     * @param currentStatus the current status that prevents booking
     */
    public EquipmentNotAvailableException(String equipmentName, EquipmentStatus currentStatus) {
        super("Equipment '" + equipmentName + "' is not available for booking. "
                + "Current status: " + currentStatus);
    }
}
