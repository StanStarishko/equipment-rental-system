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

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> findByEquipmentId(Long equipmentId) {
        return bookingRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Creates a new booking after validating all business rules.
     *
     * Checks performed in order:
     * 1. BR-05: end date must be equal to or later than start date
     * 2. BR-03: booking must reference a valid registered user
     * 3. BR-01: equipment must not be in MAINTENANCE or UNAVAILABLE status
     * 4. BR-02: no overlapping confirmed bookings for the same equipment
     * 5. BR-08: equipment status updated to BOOKED after successful creation
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
     * Cancels an existing booking.
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        booking.cancel();

        // BUG: should check if other active bookings exist for this equipment
        // and revert status to AVAILABLE if none remain.

        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking cancelled: ID {}, equipment '{}' (ID: {})",
                bookingId, booking.getEquipment().getName(), booking.getEquipment().getId());

        return cancelledBooking;
    }
}
