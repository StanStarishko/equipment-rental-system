package com.equipmentrental.equipment_rental_system.exception;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String equipmentName, String dateFrom, String dateTo) {
        super("Booking conflict: " + equipmentName
                + " already has a confirmed booking overlapping with "
                + dateFrom + " to " + dateTo);
    }
}
