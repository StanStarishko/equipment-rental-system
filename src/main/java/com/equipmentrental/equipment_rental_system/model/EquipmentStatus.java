package com.equipmentrental.equipment_rental_system.model;

/**
 * Defines the availability status of an equipment item.
 *
 * <ul>
 *   <li>{@link #AVAILABLE} - ready to be booked</li>
 *   <li>{@link #BOOKED} - has at least one active booking, but can accept non-overlapping reservations</li>
 *   <li>{@link #MAINTENANCE} - temporarily out of service, cannot be booked (BR-01)</li>
 *   <li>{@link #UNAVAILABLE} - withdrawn from use, cannot be booked (BR-01)</li>
 * </ul>
 *
 * @see Equipment
 */
public enum EquipmentStatus {
    AVAILABLE,
    BOOKED,
    MAINTENANCE,
    UNAVAILABLE
}
