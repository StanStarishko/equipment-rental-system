package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.Booking;
import com.equipmentrental.equipment_rental_system.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Booking} entities.
 * Provides standard CRUD operations via {@link JpaRepository} and custom
 * query methods for filtering and conflict detection.
 *
 * @see Booking
 * @see com.equipmentrental.equipment_rental_system.service.BookingService
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds all bookings for a specific equipment item.
     *
     * @param equipmentId the ID of the equipment item
     * @return list of bookings for the given equipment
     */
    List<Booking> findByEquipmentId(Long equipmentId);

    /**
     * Finds all bookings created by a specific user.
     *
     * @param userId the ID of the user
     * @return list of bookings created by the given user
     */
    List<Booking> findByUserId(Long userId);

    /**
     * Finds all bookings with a specific status.
     *
     * @param status the booking status to filter by
     * @return list of bookings matching the given status
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Finds confirmed bookings for a given equipment item that overlap with the specified date range.
     * Used by the service layer to enforce BR-02 (no overlapping bookings for the same item).
     *
     * <p>The overlap logic treats both start and end dates as inclusive:
     * a booking from 5 June to 10 June conflicts with one from 10 June to 15 June
     * because 10 June falls within both ranges.</p>
     *
     * <p>Only bookings with CONFIRMED status are considered; cancelled bookings are ignored.</p>
     *
     * @param equipmentId the ID of the equipment item to check
     * @param dateFrom    the start of the requested date range
     * @param dateTo      the end of the requested date range
     * @return list of conflicting confirmed bookings (empty if no conflicts)
     */
    @Query("SELECT booking FROM Booking booking " +
           "WHERE booking.equipment.id = :equipmentId " +
           "AND booking.status = 'CONFIRMED' " +
           "AND booking.dateFrom <= :dateTo " +
           "AND booking.dateTo >= :dateFrom")
    List<Booking> findConflictingBookings(@Param("equipmentId") Long equipmentId,
                                          @Param("dateFrom") LocalDate dateFrom,
                                          @Param("dateTo") LocalDate dateTo);
}
