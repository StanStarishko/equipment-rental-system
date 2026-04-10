package com.equipmentrental.equipment_rental_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a staff member who can create and manage equipment bookings.
 * Every booking must be linked to a registered user (BR-03).
 *
 * <p>The table is named {@code app_user} rather than {@code user} because
 * "user" is a reserved keyword in H2 and PostgreSQL.</p>
 *
 * @see Booking
 * @see UserRole
 * @see com.equipmentrental.equipment_rental_system.service.UserService
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String department;

    private String phone;

    @Email(message = "Please enter a valid email address")
    private String email;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Creates a new user with all required and optional fields.
     *
     * @param username   unique login identifier
     * @param password   user password (stored as plain text in this prototype)
     * @param firstName  first name of the staff member
     * @param lastName   last name of the staff member
     * @param role       access level (ADMIN or STAFF)
     * @param department organisational department
     * @param phone      contact telephone number
     * @param email      contact email address
     */
    public User(String username, String password, String firstName, String lastName,
                UserRole role, String department, String phone, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.department = department;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the user's full name by combining first and last name.
     *
     * @return the full name in "FirstName LastName" format
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
