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

    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    public Equipment findById(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));
    }

    public List<Equipment> findByCategoryId(Long categoryId) {
        return equipmentRepository.findByCategoryId(categoryId);
    }

    public List<Equipment> findByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status);
    }

    public List<Equipment> searchByName(String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Equipment save(Equipment equipment) {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment saved: '{}' (ID: {}, status: {})",
                savedEquipment.getName(), savedEquipment.getId(), savedEquipment.getStatus());
        return savedEquipment;
    }

    /**
     * Deletes equipment if it has no active (confirmed) bookings (BR-07).
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
     * Checks whether equipment is available for booking (BR-01).
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
     * Updates equipment status (BR-08).
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
