package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an equipment category used to group items by type.
 * Examples include Audio Equipment, Computing and Photography.
 *
 * <p>Each equipment item must belong to exactly one category (BR-04).
 * A category cannot be deleted while equipment items are assigned to it (BR-06).</p>
 *
 * @see Equipment
 * @see com.equipmentrental.equipment_rental_system.service.CategoryService
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    private List<Equipment> equipmentItems = new ArrayList<>();

    /**
     * Creates a new category with the specified name and description.
     *
     * @param name        the unique category name
     * @param description a brief description of the category
     */
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the number of equipment items currently assigned to this category.
     *
     * @return the count of equipment items in this category
     */
    public int getEquipmentCount() {
        return equipmentItems.size();
    }
}
