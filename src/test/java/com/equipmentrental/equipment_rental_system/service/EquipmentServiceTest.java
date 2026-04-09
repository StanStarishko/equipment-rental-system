package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.EquipmentNotAvailableException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.*;
import com.equipmentrental.equipment_rental_system.repository.BookingRepository;
import com.equipmentrental.equipment_rental_system.repository.EquipmentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService Tests")
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment testEquipment;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Computing", "Laptops and tablets");
        testCategory.setId(1L);

        testEquipment = new Equipment("Dell Laptop", "Business laptop", "IT Office",
                "Good", EquipmentStatus.AVAILABLE, LocalDate.of(2024, 6, 10),
                new BigDecimal("18.00"), testCategory);
        testEquipment.setId(1L);
    }

    @Nested
    @DisplayName("Availability Verification Tests (BR-01)")
    class AvailabilityVerificationTests {

        @Test
        @DisplayName("Should allow booking for equipment with AVAILABLE status")
        void shouldAllowBookingForAvailableEquipment() {
            testEquipment.setStatus(EquipmentStatus.AVAILABLE);

            assertDoesNotThrow(() -> equipmentService.verifyAvailableForBooking(testEquipment));
        }

        @Test
        @DisplayName("Should allow booking for equipment with BOOKED status")
        void shouldAllowBookingForBookedEquipment() {
            testEquipment.setStatus(EquipmentStatus.BOOKED);

            assertDoesNotThrow(() -> equipmentService.verifyAvailableForBooking(testEquipment));
        }

        @Test
        @DisplayName("Should reject booking for equipment under maintenance")
        void shouldRejectBookingForMaintenanceEquipment() {
            testEquipment.setStatus(EquipmentStatus.MAINTENANCE);

            EquipmentNotAvailableException exception = assertThrows(
                    EquipmentNotAvailableException.class,
                    () -> equipmentService.verifyAvailableForBooking(testEquipment));

            assertTrue(exception.getMessage().contains("MAINTENANCE"));
        }

        @Test
        @DisplayName("Should reject booking for unavailable equipment")
        void shouldRejectBookingForUnavailableEquipment() {
            testEquipment.setStatus(EquipmentStatus.UNAVAILABLE);

            EquipmentNotAvailableException exception = assertThrows(
                    EquipmentNotAvailableException.class,
                    () -> equipmentService.verifyAvailableForBooking(testEquipment));

            assertTrue(exception.getMessage().contains("UNAVAILABLE"));
        }
    }

    @Nested
    @DisplayName("Deletion Tests (BR-07)")
    class DeletionTests {

        @Test
        @DisplayName("Should delete equipment with no active bookings")
        void shouldDeleteEquipmentWithNoActiveBookings() {
            Booking cancelledBooking = new Booking();
            cancelledBooking.setStatus(BookingStatus.CANCELLED);

            when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
            when(bookingRepository.findByEquipmentId(1L)).thenReturn(List.of(cancelledBooking));

            assertDoesNotThrow(() -> equipmentService.deleteById(1L));

            verify(equipmentRepository).delete(testEquipment);
        }

        @Test
        @DisplayName("Should delete equipment with no bookings at all")
        void shouldDeleteEquipmentWithNoBookings() {
            when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
            when(bookingRepository.findByEquipmentId(1L)).thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> equipmentService.deleteById(1L));

            verify(equipmentRepository).delete(testEquipment);
        }

        @Test
        @DisplayName("Should block deletion when active bookings exist")
        void shouldBlockDeletionWhenActiveBookingsExist() {
            Booking activeBooking = new Booking();
            activeBooking.setStatus(BookingStatus.CONFIRMED);

            when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
            when(bookingRepository.findByEquipmentId(1L)).thenReturn(List.of(activeBooking));

            DeletionBlockedException exception = assertThrows(
                    DeletionBlockedException.class,
                    () -> equipmentService.deleteById(1L));

            assertTrue(exception.getMessage().contains("Dell Laptop"));
            verify(equipmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent equipment")
        void shouldThrowWhenDeletingNonExistentEquipment() {
            when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> equipmentService.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Status Update Tests (BR-08)")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should update equipment status to BOOKED")
        void shouldUpdateStatusToBooked() {
            when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
            when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            equipmentService.updateStatus(1L, EquipmentStatus.BOOKED);

            assertEquals(EquipmentStatus.BOOKED, testEquipment.getStatus());
            verify(equipmentRepository).save(testEquipment);
        }

        @Test
        @DisplayName("Should update equipment status to AVAILABLE")
        void shouldUpdateStatusToAvailable() {
            testEquipment.setStatus(EquipmentStatus.BOOKED);

            when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
            when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            equipmentService.updateStatus(1L, EquipmentStatus.AVAILABLE);

            assertEquals(EquipmentStatus.AVAILABLE, testEquipment.getStatus());
        }
    }

    @Nested
    @DisplayName("Search and Filter Tests")
    class SearchAndFilterTests {

        @Test
        @DisplayName("Should find equipment by category ID")
        void shouldFindEquipmentByCategoryId() {
            when(equipmentRepository.findByCategoryId(1L)).thenReturn(List.of(testEquipment));

            List<Equipment> results = equipmentService.findByCategoryId(1L);

            assertEquals(1, results.size());
            assertEquals("Dell Laptop", results.get(0).getName());
        }

        @Test
        @DisplayName("Should find equipment by status")
        void shouldFindEquipmentByStatus() {
            when(equipmentRepository.findByStatus(EquipmentStatus.AVAILABLE))
                    .thenReturn(List.of(testEquipment));

            List<Equipment> results = equipmentService.findByStatus(EquipmentStatus.AVAILABLE);

            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("Should search equipment by name (case-insensitive)")
        void shouldSearchEquipmentByName() {
            when(equipmentRepository.findByNameContainingIgnoreCase("dell"))
                    .thenReturn(List.of(testEquipment));

            List<Equipment> results = equipmentService.searchByName("dell");

            assertEquals(1, results.size());
            assertEquals("Dell Laptop", results.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty list when no equipment matches search")
        void shouldReturnEmptyListWhenNoMatch() {
            when(equipmentRepository.findByNameContainingIgnoreCase("nonexistent"))
                    .thenReturn(Collections.emptyList());

            List<Equipment> results = equipmentService.searchByName("nonexistent");

            assertTrue(results.isEmpty());
        }
    }
}
