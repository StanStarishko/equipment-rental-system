package com.equipmentrental.equipment_rental_system.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Booking Entity Tests")
class BookingTest {

    @Nested
    @DisplayName("Date Conflict Detection Tests")
    class DateConflictDetectionTests {

        @Test
        @DisplayName("Should detect conflict when date ranges fully overlap")
        void shouldDetectConflictWhenFullyOverlapping() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 6), LocalDate.of(2026, 5, 9));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should detect conflict when date ranges partially overlap at start")
        void shouldDetectConflictWhenPartiallyOverlappingAtStart() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 7));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should detect conflict when date ranges partially overlap at end")
        void shouldDetectConflictWhenPartiallyOverlappingAtEnd() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 15));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should detect conflict when bookings share a single boundary date")
        void shouldDetectConflictOnBoundaryDate() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 15));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should detect conflict when one booking fully contains another")
        void shouldDetectConflictWhenOneContainsAnother() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 20));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should detect conflict for same-day bookings")
        void shouldDetectConflictForSameDayBookings() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 5));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 5));

            assertTrue(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should not detect conflict when date ranges do not overlap")
        void shouldNotDetectConflictWhenNoOverlap() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking newBooking = createBooking(LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 15));

            assertFalse(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should not detect conflict when bookings are weeks apart")
        void shouldNotDetectConflictWhenWeeksApart() {
            Booking existingBooking = createBooking(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5));
            Booking newBooking = createBooking(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5));

            assertFalse(existingBooking.hasConflictWith(newBooking));
        }

        @Test
        @DisplayName("Should be symmetric: A conflicts with B means B conflicts with A")
        void shouldBeSymmetric() {
            Booking bookingA = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));
            Booking bookingB = createBooking(LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 15));

            assertEquals(bookingA.hasConflictWith(bookingB), bookingB.hasConflictWith(bookingA));
        }
    }

    @Nested
    @DisplayName("Booking Status Tests")
    class BookingStatusTests {

        @Test
        @DisplayName("Should return true for active booking with CONFIRMED status")
        void shouldReturnTrueForActiveBooking() {
            Booking booking = new Booking();
            booking.setStatus(BookingStatus.CONFIRMED);

            assertTrue(booking.isActive());
        }

        @Test
        @DisplayName("Should return false for cancelled booking")
        void shouldReturnFalseForCancelledBooking() {
            Booking booking = new Booking();
            booking.setStatus(BookingStatus.CANCELLED);

            assertFalse(booking.isActive());
        }

        @Test
        @DisplayName("Should set status to CANCELLED when cancel is called")
        void shouldSetStatusToCancelledWhenCancelCalled() {
            Booking booking = new Booking();
            booking.setStatus(BookingStatus.CONFIRMED);

            booking.cancel();

            assertEquals(BookingStatus.CANCELLED, booking.getStatus());
            assertFalse(booking.isActive());
        }
    }

    @Nested
    @DisplayName("Duration Calculation Tests")
    class DurationCalculationTests {

        @Test
        @DisplayName("Should return 1 for same-day booking")
        void shouldReturnOneForSameDayBooking() {
            Booking booking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 5));

            assertEquals(1, booking.getDurationDays());
        }

        @Test
        @DisplayName("Should return correct duration for multi-day booking")
        void shouldReturnCorrectDurationForMultiDayBooking() {
            Booking booking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 10));

            assertEquals(6, booking.getDurationDays());
        }

        @Test
        @DisplayName("Should return correct duration for two-day booking")
        void shouldReturnCorrectDurationForTwoDayBooking() {
            Booking booking = createBooking(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 6));

            assertEquals(2, booking.getDurationDays());
        }
    }

    private Booking createBooking(LocalDate dateFrom, LocalDate dateTo) {
        Booking booking = new Booking();
        booking.setDateFrom(dateFrom);
        booking.setDateTo(dateTo);
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }
}
