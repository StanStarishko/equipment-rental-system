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

    public boolean isAvailable() {
        return status == EquipmentStatus.AVAILABLE;
    }

    public void updateStatus(EquipmentStatus newStatus) {
        this.status = newStatus;
    }
}
