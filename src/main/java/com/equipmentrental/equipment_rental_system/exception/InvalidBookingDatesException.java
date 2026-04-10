package com.equipmentrental.equipment_rental_system.exception;

/**
 * Thrown when a booking is submitted with an invalid date range (BR-05).
 * The end date must be equal to or later than the start date.
 *
 * @see com.equipmentrental.equipment_rental_system.service.BookingService#createBooking
 */
public class InvalidBookingDatesException extends RuntimeException {

    /**
     * Creates a new exception describing the invalid date range.
     *
     * @param dateFrom the submitted start date
     * @param dateTo   the submitted end date (which is before the start date)
     */
    public InvalidBookingDatesException(String dateFrom, String dateTo) {
        super("Invalid booking dates: end date (" + dateTo
                + ") must be equal to or later than start date (" + dateFrom + ")");
    }
}
