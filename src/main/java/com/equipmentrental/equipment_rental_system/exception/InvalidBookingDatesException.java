package com.equipmentrental.equipment_rental_system.exception;

public class InvalidBookingDatesException extends RuntimeException {

    public InvalidBookingDatesException(String dateFrom, String dateTo) {
        super("Invalid booking dates: end date (" + dateTo
                + ") must be equal to or later than start date (" + dateFrom + ")");
    }
}
