package com.equipmentrental.equipment_rental_system.exception;

/**
 * Thrown when a new booking conflicts with an existing confirmed booking
 * for the same equipment item (BR-02). The date ranges overlap and the
 * reservation cannot proceed.
 *
 * @see com.equipmentrental.equipment_rental_system.service.BookingService#createBooking
 */
public class BookingConflictException extends RuntimeException {

    /**
     * Creates a new exception describing the conflict.
     *
     * @param equipmentName the name of the equipment item
     * @param dateFrom      the requested start date
     * @param dateTo        the requested end date
     */
    public BookingConflictException(String equipmentName, String dateFrom, String dateTo) {
        super("Booking conflict: " + equipmentName
                + " already has a confirmed booking overlapping with "
                + dateFrom + " to " + dateTo);
    }
}
