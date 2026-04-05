package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.Booking;
import com.equipmentrental.equipment_rental_system.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByEquipmentId(Long equipmentId);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    /**
     * Finds confirmed bookings for a given equipment item that overlap with the specified date range.
     * Used by the service layer to enforce BR-02 (no overlapping bookings for the same item).
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
