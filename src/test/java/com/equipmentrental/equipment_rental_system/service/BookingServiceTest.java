package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.*;
import com.equipmentrental.equipment_rental_system.model.*;
import com.equipmentrental.equipment_rental_system.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    private Equipment testEquipment;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Computing", "Laptops and tablets");
        testCategory.setId(1L);

        testEquipment = new Equipment("Dell Laptop", "Business laptop", "IT Office",
                "Good", EquipmentStatus.AVAILABLE, LocalDate.of(2024, 6, 10),
                new BigDecimal("18.00"), testCategory);
        testEquipment.setId(1L);

        testUser = new User("jthompson", "staff123", "James", "Thompson",
                UserRole.STAFF, "Events", "0141 552 1002", "j.thompson@org.co.uk");
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("Create Booking Tests")
    class CreateBookingTests {

        @Test
        @DisplayName("Should create booking when all business rules pass")
        void shouldCreateBookingWhenAllRulesPass() {
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            Booking result = bookingService.createBooking(newBooking);

            assertNotNull(result);
            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
            verify(equipmentService).updateStatus(1L, EquipmentStatus.BOOKED);
        }

        @Test
        @DisplayName("Should set equipment status to BOOKED after successful booking (BR-08)")
        void shouldUpdateEquipmentStatusToBookedAfterCreation() {
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            bookingService.createBooking(newBooking);

            verify(equipmentService, times(1)).updateStatus(1L, EquipmentStatus.BOOKED);
        }
    }

    @Nested
    @DisplayName("Date Validation Tests (BR-05)")
    class DateValidationTests {

        @Test
        @DisplayName("Should reject booking when end date is before start date")
        void shouldRejectWhenEndDateBeforeStartDate() {
            Booking invalidBooking = new Booking();
            invalidBooking.setDateFrom(LocalDate.of(2026, 6, 15));
            invalidBooking.setDateTo(LocalDate.of(2026, 6, 10));
            invalidBooking.setEquipment(testEquipment);
            invalidBooking.setUser(testUser);

            assertThrows(InvalidBookingDatesException.class,
                    () -> bookingService.createBooking(invalidBooking));

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should accept booking when start date equals end date")
        void shouldAcceptWhenStartDateEqualsEndDate() {
            Booking sameDayBooking = new Booking();
            sameDayBooking.setDateFrom(LocalDate.of(2026, 6, 15));
            sameDayBooking.setDateTo(LocalDate.of(2026, 6, 15));
            sameDayBooking.setPurpose("Quick task");
            sameDayBooking.setEquipment(testEquipment);
            sameDayBooking.setUser(testUser);

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Booking result = bookingService.createBooking(sameDayBooking);

            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Equipment Availability Tests (BR-01)")
    class EquipmentAvailabilityTests {

        @Test
        @DisplayName("Should reject booking when equipment is under maintenance")
        void shouldRejectWhenEquipmentUnderMaintenance() {
            Equipment maintenanceEquipment = new Equipment();
            maintenanceEquipment.setId(2L);
            maintenanceEquipment.setName("Broken Laptop");
            maintenanceEquipment.setStatus(EquipmentStatus.MAINTENANCE);

            Booking newBooking = buildValidBooking();
            newBooking.setEquipment(maintenanceEquipment);

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(2L)).thenReturn(maintenanceEquipment);
            doThrow(new EquipmentNotAvailableException("Broken Laptop", EquipmentStatus.MAINTENANCE))
                    .when(equipmentService).verifyAvailableForBooking(maintenanceEquipment);

            assertThrows(EquipmentNotAvailableException.class,
                    () -> bookingService.createBooking(newBooking));

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject booking when equipment is unavailable")
        void shouldRejectWhenEquipmentUnavailable() {
            Equipment unavailableEquipment = new Equipment();
            unavailableEquipment.setId(3L);
            unavailableEquipment.setName("Retired Camera");
            unavailableEquipment.setStatus(EquipmentStatus.UNAVAILABLE);

            Booking newBooking = buildValidBooking();
            newBooking.setEquipment(unavailableEquipment);

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(3L)).thenReturn(unavailableEquipment);
            doThrow(new EquipmentNotAvailableException("Retired Camera", EquipmentStatus.UNAVAILABLE))
                    .when(equipmentService).verifyAvailableForBooking(unavailableEquipment);

            assertThrows(EquipmentNotAvailableException.class,
                    () -> bookingService.createBooking(newBooking));
        }

        @Test
        @DisplayName("Should allow booking when equipment status is AVAILABLE")
        void shouldAllowBookingWhenEquipmentAvailable() {
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Booking result = bookingService.createBooking(newBooking);

            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        }

        @Test
        @DisplayName("Should allow booking when equipment status is BOOKED but no date conflict")
        void shouldAllowBookingWhenBookedButNoDatesConflict() {
            testEquipment.setStatus(EquipmentStatus.BOOKED);
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Booking result = bookingService.createBooking(newBooking);

            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Booking Conflict Tests (BR-02)")
    class BookingConflictTests {

        @Test
        @DisplayName("Should reject booking when conflicting booking exists")
        void shouldRejectWhenConflictingBookingExists() {
            Booking existingBooking = new Booking();
            existingBooking.setId(99L);
            existingBooking.setStatus(BookingStatus.CONFIRMED);

            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(List.of(existingBooking));

            assertThrows(BookingConflictException.class,
                    () -> bookingService.createBooking(newBooking));

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow booking when no conflicting bookings exist")
        void shouldAllowWhenNoConflictingBookings() {
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L)).thenReturn(testUser);
            when(equipmentService.findById(1L)).thenReturn(testEquipment);
            when(bookingRepository.findConflictingBookings(eq(1L), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Booking result = bookingService.createBooking(newBooking);

            assertNotNull(result);
            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("User Validation Tests (BR-03)")
    class UserValidationTests {

        @Test
        @DisplayName("Should reject booking when user does not exist")
        void shouldRejectWhenUserDoesNotExist() {
            Booking newBooking = buildValidBooking();

            when(userService.findById(1L))
                    .thenThrow(new ResourceNotFoundException("User", 1L));

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.createBooking(newBooking));

            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cancel Booking Tests")
    class CancelBookingTests {

        @Test
        @DisplayName("Should set booking status to CANCELLED")
        void shouldSetStatusToCancelled() {
            Booking activeBooking = buildValidBooking();
            activeBooking.setId(1L);
            activeBooking.setStatus(BookingStatus.CONFIRMED);
            activeBooking.setEquipment(testEquipment);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(activeBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Booking result = bookingService.cancelBooking(1L);

            assertEquals(BookingStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-existent booking")
        void shouldThrowWhenCancellingNonExistentBooking() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.cancelBooking(999L));
        }
    }

    @Nested
    @DisplayName("Find Booking Tests")
    class FindBookingTests {

        @Test
        @DisplayName("Should return booking when found by ID")
        void shouldReturnBookingWhenFoundById() {
            Booking booking = buildValidBooking();
            booking.setId(1L);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            Booking result = bookingService.findById(1L);

            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Should throw exception when booking not found by ID")
        void shouldThrowWhenBookingNotFoundById() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bookingService.findById(999L));
        }

        @Test
        @DisplayName("Should return all bookings for a given user")
        void shouldReturnBookingsForUser() {
            Booking booking1 = buildValidBooking();
            Booking booking2 = buildValidBooking();

            when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking1, booking2));

            List<Booking> results = bookingService.findByUserId(1L);

            assertEquals(2, results.size());
        }
    }

    private Booking buildValidBooking() {
        Booking booking = new Booking();
        booking.setDateFrom(LocalDate.of(2026, 6, 10));
        booking.setDateTo(LocalDate.of(2026, 6, 15));
        booking.setPurpose("Staff training session");
        booking.setEquipment(testEquipment);
        booking.setUser(testUser);
        return booking;
    }
}
