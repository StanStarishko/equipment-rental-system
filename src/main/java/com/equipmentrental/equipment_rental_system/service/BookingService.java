package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.BookingConflictException;
import com.equipmentrental.equipment_rental_system.exception.InvalidBookingDatesException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.*;
import com.equipmentrental.equipment_rental_system.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Booking} entities.
 * This is the central service in the application, orchestrating all booking-related
 * business rules by coordinating with {@link EquipmentService} and {@link UserService}.
 *
 * <p>Business rules enforced:</p>
 * <ul>
 *   <li>BR-01: equipment in MAINTENANCE or UNAVAILABLE status cannot be booked
 *       (delegated to {@link EquipmentService#verifyAvailableForBooking})</li>
 *   <li>BR-02: overlapping bookings for the same equipment are rejected
 *       (via {@link BookingRepository#findConflictingBookings})</li>
 *   <li>BR-03: every booking must reference a registered user</li>
 *   <li>BR-05: end date must be equal to or later than start date</li>
 *   <li>BR-08: equipment status is updated when bookings are created or cancelled
 *       (delegated to {@link EquipmentService#updateStatus})</li>
 * </ul>
 *
 * @see Booking
 * @see BookingRepository
 * @see EquipmentService
 * @see UserService
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final EquipmentService equipmentService;
    private final UserService userService;

    public BookingService(BookingRepository bookingRepository,
                          EquipmentService equipmentService,
                          UserService userService) {
        this.bookingRepository = bookingRepository;
        this.equipmentService = equipmentService;
        this.userService = userService;
    }

    /**
     * Returns all bookings in the system.
     *
     * @return list of all bookings
     */
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    /**
     * Finds a booking by its ID.
     *
     * @param bookingId the ID of the booking to find
     * @return the booking
     * @throws ResourceNotFoundException if no booking exists with the given ID
     */
    public Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    /**
     * Finds all bookings created by a specific user.
     *
     * @param userId the ID of the user
     * @return list of bookings for the given user
     */
    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    /**
     * Finds all bookings for a specific equipment item.
     *
     * @param equipmentId the ID of the equipment
     * @return list of bookings for the given equipment
     */
    public List<Booking> findByEquipmentId(Long equipmentId) {
        return bookingRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Creates a new booking after validating all business rules.
     *
     * <p>Checks are performed in the following order:</p>
     * <ol>
     *   <li>BR-05: end date must be equal to or later than start date</li>
     *   <li>BR-03: the user must exist in the system</li>
     *   <li>BR-01: the equipment must not be in MAINTENANCE or UNAVAILABLE status</li>
     *   <li>BR-02: no overlapping confirmed bookings may exist for the same equipment</li>
     * </ol>
     *
     * <p>If all checks pass, the booking is saved with CONFIRMED status and the
     * equipment status is updated to BOOKED (BR-08).</p>
     *
     * @param booking the booking to create (must have equipment and user set)
     * @return the saved booking with generated ID and CONFIRMED status
     * @throws InvalidBookingDatesException if the end date is before the start date
     * @throws ResourceNotFoundException   if the user or equipment does not exist
     * @throws EquipmentNotAvailableException if the equipment is in MAINTENANCE or UNAVAILABLE status
     * @throws BookingConflictException     if an overlapping confirmed booking exists
     */
    @Transactional
    public Booking createBooking(Booking booking) {

        // BR-05: validate date range
        if (booking.getDateFrom().isAfter(booking.getDateTo())) {
            log.warn("Booking rejected: invalid date range {} to {}",
                    booking.getDateFrom(), booking.getDateTo());
            throw new InvalidBookingDatesException(
                    booking.getDateFrom().toString(),
                    booking.getDateTo().toString());
        }

        // BR-03: verify user exists
        User bookingUser = userService.findById(booking.getUser().getId());

        // BR-01: verify equipment is available for booking
        Equipment equipment = equipmentService.findById(booking.getEquipment().getId());
        equipmentService.verifyAvailableForBooking(equipment);

        // BR-02: check for conflicting bookings
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                equipment.getId(),
                booking.getDateFrom(),
                booking.getDateTo());

        if (!conflictingBookings.isEmpty()) {
            log.warn("Booking rejected: conflict detected for equipment '{}' (ID: {}) on dates {} to {}",
                    equipment.getName(), equipment.getId(),
                    booking.getDateFrom(), booking.getDateTo());
            throw new BookingConflictException(
                    equipment.getName(),
                    booking.getDateFrom().toString(),
                    booking.getDateTo().toString());
        }

        // All checks passed: save the booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setEquipment(equipment);
        booking.setUser(bookingUser);
        Booking savedBooking = bookingRepository.save(booking);

        // BR-08: update equipment status to reflect the active booking
        equipmentService.updateStatus(equipment.getId(), EquipmentStatus.BOOKED);

        log.info("Booking created: ID {}, equipment '{}', user '{}', dates {} to {}",
                savedBooking.getId(), equipment.getName(), bookingUser.getFullName(),
                savedBooking.getDateFrom(), savedBooking.getDateTo());

        return savedBooking;
    }

    /**
     * Cancels an existing booking and updates the equipment status if appropriate (BR-08).
     *
     * <p>After setting the booking status to CANCELLED, this method checks whether
     * any other active (confirmed) bookings remain for the same equipment item.
     * If none remain, the equipment status is reverted to AVAILABLE.</p>
     *
     * @param bookingId the ID of the booking to cancel
     * @return the cancelled booking
     * @throws ResourceNotFoundException if no booking exists with the given ID
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        booking.cancel();
        Booking cancelledBooking = bookingRepository.save(booking);

        // BR-08: check if equipment should revert to AVAILABLE
        Long equipmentId = booking.getEquipment().getId();
        boolean hasOtherActiveBookings = bookingRepository.findByEquipmentId(equipmentId).stream()
                .anyMatch(otherBooking -> otherBooking.isActive() && !otherBooking.getId().equals(bookingId));

        if (!hasOtherActiveBookings) {
            equipmentService.updateStatus(equipmentId, EquipmentStatus.AVAILABLE);
        }

        log.info("Booking cancelled: ID {}, equipment '{}' (ID: {})",
                bookingId, booking.getEquipment().getName(), equipmentId);

        return cancelledBooking;
    }
}
