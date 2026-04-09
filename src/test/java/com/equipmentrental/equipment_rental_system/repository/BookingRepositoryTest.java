package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("BookingRepository Integration Tests")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Equipment testEquipment;
    private User testUser;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(new Category("Computing", "Laptops"));

        testEquipment = equipmentRepository.save(new Equipment(
                "Dell Laptop", "Business laptop", "IT Office", "Good",
                EquipmentStatus.AVAILABLE, LocalDate.of(2024, 6, 10),
                new BigDecimal("18.00"), category));

        testUser = userRepository.save(new User(
                "jthompson", "staff123", "James", "Thompson",
                UserRole.STAFF, "Events", "0141 552 1002", "j.thompson@org.co.uk"));
    }

    @Nested
    @DisplayName("Conflict Detection Query Tests (BR-02)")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should find conflicting booking when dates fully overlap")
        void shouldFindConflictWhenDatesFullyOverlap() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 7),
                    LocalDate.of(2026, 6, 9));

            assertEquals(1, conflicts.size());
        }

        @Test
        @DisplayName("Should find conflicting booking when dates partially overlap")
        void shouldFindConflictWhenDatesPartiallyOverlap() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 8),
                    LocalDate.of(2026, 6, 15));

            assertEquals(1, conflicts.size());
        }

        @Test
        @DisplayName("Should find conflicting booking on shared boundary date")
        void shouldFindConflictOnBoundaryDate() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 15));

            assertEquals(1, conflicts.size());
        }

        @Test
        @DisplayName("Should not find conflict when dates do not overlap")
        void shouldNotFindConflictWhenNoOverlap() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 11),
                    LocalDate.of(2026, 6, 15));

            assertTrue(conflicts.isEmpty());
        }

        @Test
        @DisplayName("Should ignore cancelled bookings when checking conflicts")
        void shouldIgnoreCancelledBookings() {
            Booking cancelledBooking = new Booking(
                    LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10),
                    "Cancelled event", BookingStatus.CANCELLED, testEquipment, testUser);
            bookingRepository.save(cancelledBooking);

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 7),
                    LocalDate.of(2026, 6, 9));

            assertTrue(conflicts.isEmpty());
        }

        @Test
        @DisplayName("Should not find conflict for different equipment")
        void shouldNotFindConflictForDifferentEquipment() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            Category otherCategory = categoryRepository.save(new Category("Audio", "Speakers"));
            Equipment otherEquipment = equipmentRepository.save(new Equipment(
                    "Microphone", "Wireless", "Room B", "Good",
                    EquipmentStatus.AVAILABLE, LocalDate.of(2024, 1, 1),
                    new BigDecimal("12.00"), otherCategory));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    otherEquipment.getId(),
                    LocalDate.of(2026, 6, 5),
                    LocalDate.of(2026, 6, 10));

            assertTrue(conflicts.isEmpty());
        }

        @Test
        @DisplayName("Should find multiple conflicting bookings")
        void shouldFindMultipleConflictingBookings() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 8));
            persistConfirmedBooking(LocalDate.of(2026, 6, 9), LocalDate.of(2026, 6, 12));

            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    testEquipment.getId(),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30));

            assertEquals(2, conflicts.size());
        }
    }

    @Nested
    @DisplayName("Query Method Tests")
    class QueryMethodTests {

        @Test
        @DisplayName("Should find bookings by equipment ID")
        void shouldFindBookingsByEquipmentId() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            List<Booking> results = bookingRepository.findByEquipmentId(testEquipment.getId());

            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("Should find bookings by user ID")
        void shouldFindBookingsByUserId() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));
            persistConfirmedBooking(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));

            List<Booking> results = bookingRepository.findByUserId(testUser.getId());

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find bookings by status")
        void shouldFindBookingsByStatus() {
            persistConfirmedBooking(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10));

            Booking cancelledBooking = new Booking(
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5),
                    "Cancelled", BookingStatus.CANCELLED, testEquipment, testUser);
            bookingRepository.save(cancelledBooking);

            List<Booking> confirmedResults = bookingRepository.findByStatus(BookingStatus.CONFIRMED);
            List<Booking> cancelledResults = bookingRepository.findByStatus(BookingStatus.CANCELLED);

            assertEquals(1, confirmedResults.size());
            assertEquals(1, cancelledResults.size());
        }
    }

    private void persistConfirmedBooking(LocalDate dateFrom, LocalDate dateTo) {
        Booking booking = new Booking(dateFrom, dateTo, "Test purpose",
                BookingStatus.CONFIRMED, testEquipment, testUser);
        bookingRepository.save(booking);
    }
}