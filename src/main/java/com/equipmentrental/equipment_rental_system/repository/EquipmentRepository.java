package com.equipmentrental.equipment_rental_system.repository;

import com.equipmentrental.equipment_rental_system.model.Equipment;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByCategoryId(Long categoryId);

    List<Equipment> findByStatus(EquipmentStatus status);

    List<Equipment> findByNameContainingIgnoreCase(String name);
}
