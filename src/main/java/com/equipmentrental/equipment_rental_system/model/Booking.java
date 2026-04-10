package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Represents a reservation of an equipment item for a specific date range.
 * Each booking is linked to one {@link Equipment} item and one {@link User}.
 *
 * <p>Business rules enforced through this entity and the service layer:</p>
 * <ul>
 *   <li>BR-01: cannot book equipment in MAINTENANCE or UNAVAILABLE status</li>
 *   <li>BR-02: date ranges must not overlap with other confirmed bookings for the same item</li>
 *   <li>BR-03: every booking must reference a registered staff member</li>
 *   <li>BR-05: end date must be equal to or later than start date</li>
 *   <li>BR-08: equipment status is updated when bookings are created or cancelled</li>
 * </ul>
 *
 * @see Equipment
 * @see User
 * @see BookingStatus
 * @see com.equipmentrental.equipment_rental_system.service.BookingService
 */
@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dateFrom;

    @NotNull(message = "End date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dateTo;

    @NotBlank(message = "Booking purpose is required")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Creates a new booking with all required fields.
     *
     * @param dateFrom  the first day of the reservation (inclusive)
     * @param dateTo    the last day of the reservation (inclusive)
     * @param purpose   reason for the booking
     * @param status    initial booking status (typically CONFIRMED)
     * @param equipment the equipment item being reserved
     * @param user      the staff member creating the booking
     */
    public Booking(LocalDate dateFrom, LocalDate dateTo, String purpose,
                   BookingStatus status, Equipment equipment, User user) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.purpose = purpose;
        this.status = status;
        this.equipment = equipment;
        this.user = user;
    }

    /**
     * Automatically sets the creation timestamp when the booking is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Checks whether this booking's date range overlaps with another booking.
     * Both start and end dates are treated as inclusive, so a booking ending on
     * 10 April conflicts with one starting on 10 April.
     *
     * <p>This method only compares dates. It does not check whether the bookings
     * refer to the same equipment or whether either booking is cancelled.
     * Those checks are the responsibility of the service layer.</p>
     *
     * @param otherBooking the booking to compare against
     * @return {@code true} if the date ranges overlap
     */
    public boolean hasConflictWith(Booking otherBooking) {
        return !this.dateFrom.isAfter(otherBooking.dateTo)
                && !otherBooking.dateFrom.isAfter(this.dateTo);
    }

    /**
     * Cancels this booking by setting its status to {@link BookingStatus#CANCELLED}.
     */
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    /**
     * Checks whether this booking is currently active (confirmed and not cancelled).
     *
     * @return {@code true} if the status is {@link BookingStatus#CONFIRMED}
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED;
    }

    /**
     * Calculates the duration of this booking in days (inclusive of both start and end dates).
     * A same-day booking returns 1.
     *
     * @return the number of days in the booking period
     */
    public int getDurationDays() {
        return (int) ChronoUnit.DAYS.between(dateFrom, dateTo) + 1;
    }
}
