package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.Category;
import com.equipmentrental.equipment_rental_system.repository.CategoryRepository;
import com.equipmentrental.equipment_rental_system.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           EquipmentRepository equipmentRepository) {
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Deletes a category if no equipment items are assigned to it (BR-06).
     */
    @Transactional
    public void deleteById(Long categoryId) {
        Category category = findById(categoryId);

        boolean hasEquipment = !equipmentRepository.findByCategoryId(categoryId).isEmpty();
        if (hasEquipment) {
            throw new DeletionBlockedException(
                    "category '" + category.getName() + "'",
                    "there are equipment items still assigned to this category");
        }

        categoryRepository.delete(category);
    }
}
