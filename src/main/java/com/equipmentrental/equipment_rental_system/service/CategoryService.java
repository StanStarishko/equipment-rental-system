package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.Category;
import com.equipmentrental.equipment_rental_system.repository.CategoryRepository;
import com.equipmentrental.equipment_rental_system.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Category} entities.
 * Handles CRUD operations and enforces BR-06 (a category cannot be deleted
 * while equipment items are assigned to it).
 *
 * @see Category
 * @see CategoryRepository
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           EquipmentRepository equipmentRepository) {
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Returns all categories in the system.
     *
     * @return list of all categories
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Finds a category by its ID.
     *
     * @param categoryId the ID of the category to find
     * @return the category
     * @throws ResourceNotFoundException if no category exists with the given ID
     */
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    /**
     * Saves a new or updated category.
     *
     * @param category the category to save
     * @return the saved category with generated ID (if new)
     */
    @Transactional
    public Category save(Category category) {
        Category savedCategory = categoryRepository.save(category);
        log.info("Category saved: '{}' (ID: {})", savedCategory.getName(), savedCategory.getId());
        return savedCategory;
    }

    /**
     * Deletes a category if no equipment items are assigned to it (BR-06).
     *
     * @param categoryId the ID of the category to delete
     * @throws ResourceNotFoundException if no category exists with the given ID
     * @throws DeletionBlockedException  if equipment items are still assigned to this category
     */
    @Transactional
    public void deleteById(Long categoryId) {
        Category category = findById(categoryId);

        boolean hasEquipment = !equipmentRepository.findByCategoryId(categoryId).isEmpty();
        if (hasEquipment) {
            log.warn("Deletion blocked for category '{}' (ID: {}): equipment items still assigned",
                    category.getName(), categoryId);
            throw new DeletionBlockedException(
                    "category '" + category.getName() + "'",
                    "there are equipment items still assigned to this category");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: '{}' (ID: {})", category.getName(), categoryId);
    }
}
