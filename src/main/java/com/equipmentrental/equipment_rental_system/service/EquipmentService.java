package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.EquipmentNotAvailableException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.BookingStatus;
import com.equipmentrental.equipment_rental_system.model.Equipment;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import com.equipmentrental.equipment_rental_system.repository.BookingRepository;
import com.equipmentrental.equipment_rental_system.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Equipment} entities.
 * Handles CRUD operations, search and filtering, and enforces the following business rules:
 * <ul>
 *   <li>BR-01: equipment in MAINTENANCE or UNAVAILABLE status cannot be booked</li>
 *   <li>BR-07: equipment with active bookings cannot be deleted</li>
 *   <li>BR-08: equipment status is updated to reflect active bookings</li>
 * </ul>
 *
 * <p>This service is called by {@link BookingService} for availability verification
 * and status updates, following the architectural principle that each service manages
 * its own entity.</p>
 *
 * @see Equipment
 * @see EquipmentRepository
 * @see BookingService
 */
@Service
@Transactional(readOnly = true)
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            BookingRepository bookingRepository) {
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns all equipment items in the system.
     *
     * @return list of all equipment
     */
    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    /**
     * Finds an equipment item by its ID.
     *
     * @param equipmentId the ID of the equipment to find
     * @return the equipment item
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     */
    public Equipment findById(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));
    }

    /**
     * Finds all equipment items belonging to a specific category.
     *
     * @param categoryId the ID of the category to filter by
     * @return list of equipment in the given category
     */
    public List<Equipment> findByCategoryId(Long categoryId) {
        return equipmentRepository.findByCategoryId(categoryId);
    }

    /**
     * Finds all equipment items with a specific availability status.
     *
     * @param status the status to filter by
     * @return list of equipment matching the given status
     */
    public List<Equipment> findByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status);
    }

    /**
     * Searches for equipment items whose name contains the given text (case-insensitive).
     *
     * @param name the search text
     * @return list of matching equipment items
     */
    public List<Equipment> searchByName(String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Saves a new or updated equipment item.
     *
     * @param equipment the equipment to save
     * @return the saved equipment with generated ID (if new)
     */
    @Transactional
    public Equipment save(Equipment equipment) {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment saved: '{}' (ID: {}, status: {})",
                savedEquipment.getName(), savedEquipment.getId(), savedEquipment.getStatus());
        return savedEquipment;
    }

    /**
     * Deletes an equipment item if it has no active (confirmed) bookings (BR-07).
     *
     * @param equipmentId the ID of the equipment to delete
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     * @throws DeletionBlockedException  if the equipment has active bookings
     */
    @Transactional
    public void deleteById(Long equipmentId) {
        Equipment equipment = findById(equipmentId);

        boolean hasActiveBookings = bookingRepository.findByEquipmentId(equipmentId).stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.CONFIRMED);

        if (hasActiveBookings) {
            log.warn("Deletion blocked for equipment '{}' (ID: {}): active bookings exist",
                    equipment.getName(), equipmentId);
            throw new DeletionBlockedException(
                    "equipment '" + equipment.getName() + "'",
                    "there are active bookings for this item that must be cancelled first");
        }

        equipmentRepository.delete(equipment);
        log.info("Equipment deleted: '{}' (ID: {})", equipment.getName(), equipmentId);
    }

    /**
     * Verifies that an equipment item is available for booking (BR-01).
     * Equipment must not be in MAINTENANCE or UNAVAILABLE status.
     * Called by {@link BookingService#createBooking} before creating a new booking.
     *
     * @param equipment the equipment item to check
     * @throws EquipmentNotAvailableException if the equipment status prevents booking
     */
    public void verifyAvailableForBooking(Equipment equipment) {
        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE
                || equipment.getStatus() == EquipmentStatus.UNAVAILABLE) {
            log.warn("Booking rejected: equipment '{}' (ID: {}) has status {}",
                    equipment.getName(), equipment.getId(), equipment.getStatus());
            throw new EquipmentNotAvailableException(equipment.getName(), equipment.getStatus());
        }
    }

    /**
     * Updates the availability status of an equipment item (BR-08).
     * Called by {@link BookingService} when bookings are created (status set to BOOKED)
     * or cancelled (status reverted to AVAILABLE if no other active bookings remain).
     *
     * @param equipmentId the ID of the equipment to update
     * @param newStatus   the new status to set
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     */
    @Transactional
    public void updateStatus(Long equipmentId, EquipmentStatus newStatus) {
        Equipment equipment = findById(equipmentId);
        EquipmentStatus previousStatus = equipment.getStatus();
        equipment.updateStatus(newStatus);
        equipmentRepository.save(equipment);
        log.info("Equipment status updated: '{}' (ID: {}) {} -> {}",
                equipment.getName(), equipmentId, previousStatus, newStatus);
    }
}
