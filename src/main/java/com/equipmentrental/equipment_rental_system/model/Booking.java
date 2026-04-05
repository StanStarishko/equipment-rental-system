package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateFrom;

    @Column(nullable = false)
    private LocalDate dateTo;

    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Booking(LocalDate dateFrom, LocalDate dateTo, String purpose,
                   BookingStatus status, Equipment equipment, User user) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.purpose = purpose;
        this.status = status;
        this.equipment = equipment;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Checks whether this booking's date range overlaps with another booking.
     * Both start and end dates are inclusive, so [Apr 5 - Apr 10] conflicts with [Apr 10 - Apr 15].
     * Does not check equipment or status; that responsibility belongs to the service layer.
     */
    public boolean hasConflictWith(Booking otherBooking) {
        return !this.dateFrom.isAfter(otherBooking.dateTo)
                && !otherBooking.dateFrom.isAfter(this.dateTo);
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == BookingStatus.CONFIRMED;
    }

    public int getDurationDays() {
        return (int) ChronoUnit.DAYS.between(dateFrom, dateTo) + 1;
    }
}
