package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical equipment item available for booking.
 * This is the central entity in the system: it is categorised, booked and tracked.
 *
 * <p>Each item belongs to exactly one {@link Category} (BR-04) and can have
 * multiple {@link Booking} records. The {@link EquipmentStatus} reflects the
 * current availability and is updated automatically when bookings are created
 * or cancelled (BR-08).</p>
 *
 * <p>Equipment with active bookings cannot be deleted (BR-07).
 * Items in MAINTENANCE or UNAVAILABLE status cannot be booked (BR-01).</p>
 *
 * @see Category
 * @see Booking
 * @see EquipmentStatus
 * @see com.equipmentrental.equipment_rental_system.service.EquipmentService
 */
@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    private String condition;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.00", message = "Cost per day must not be negative")
    private BigDecimal costPerDay;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "equipment")
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Creates a new equipment item with all fields.
     *
     * @param name         display name of the item
     * @param description  brief description
     * @param location     physical storage location
     * @param condition    current physical condition (e.g. Good, Fair, Excellent)
     * @param status       availability status
     * @param purchaseDate date the item was acquired
     * @param costPerDay   daily booking cost in pounds sterling
     * @param category     the category this item belongs to
     */
    public Equipment(String name, String description, String location, String condition,
                     EquipmentStatus status, LocalDate purchaseDate, BigDecimal costPerDay,
                     Category category) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.condition = condition;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.costPerDay = costPerDay;
        this.category = category;
    }

    /**
     * Checks whether this equipment item has AVAILABLE status.
     *
     * @return {@code true} if the status is {@link EquipmentStatus#AVAILABLE}
     */
    public boolean isAvailable() {
        return status == EquipmentStatus.AVAILABLE;
    }

    /**
     * Updates the equipment status to the specified value.
     *
     * @param newStatus the new status to set
     */
    public void updateStatus(EquipmentStatus newStatus) {
        this.status = newStatus;
    }
}
