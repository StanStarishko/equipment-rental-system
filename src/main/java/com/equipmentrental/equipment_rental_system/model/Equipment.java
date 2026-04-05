package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String location;

    private String condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    private LocalDate purchaseDate;

    private BigDecimal costPerDay;

    @ManyToOne(fetch = FetchType.LAZY)
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
