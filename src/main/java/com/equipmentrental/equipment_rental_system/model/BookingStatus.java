package com.equipmentrental.equipment_rental_system.model;

/**
 * Defines the lifecycle status of a booking.
 *
 * <ul>
 *   <li>{@link #CONFIRMED} - the booking is active and the equipment is reserved</li>
 *   <li>{@link #CANCELLED} - the booking has been cancelled and no longer reserves the equipment</li>
 * </ul>
 *
 * @see Booking
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED
}
