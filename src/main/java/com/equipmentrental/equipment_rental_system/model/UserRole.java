package com.equipmentrental.equipment_rental_system.model;

/**
 * Defines the access level of a system user.
 *
 * <ul>
 *   <li>{@link #ADMIN} - full access to all features including user management</li>
 *   <li>{@link #STAFF} - can create and manage bookings and view equipment</li>
 * </ul>
 *
 * @see User
 */
public enum UserRole {
    ADMIN,
    STAFF
}
