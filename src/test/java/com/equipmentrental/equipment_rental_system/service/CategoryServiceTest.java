package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.Category;
import com.equipmentrental.equipment_rental_system.model.Equipment;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import com.equipmentrental.equipment_rental_system.repository.CategoryRepository;
import com.equipmentrental.equipment_rental_system.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Audio Equipment", "Microphones, speakers and mixers");
        testCategory.setId(1L);
    }

    @Nested
    @DisplayName("Find Category Tests")
    class FindCategoryTests {

        @Test
        @DisplayName("Should return category when found by ID")
        void shouldReturnCategoryWhenFoundById() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

            Category result = categoryService.findById(1L);

            assertEquals("Audio Equipment", result.getName());
        }

        @Test
        @DisplayName("Should throw exception when category not found by ID")
        void shouldThrowWhenCategoryNotFoundById() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.findById(999L));
        }

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() {
            Category secondCategory = new Category("Computing", "Laptops and tablets");
            secondCategory.setId(2L);

            when(categoryRepository.findAll()).thenReturn(List.of(testCategory, secondCategory));

            List<Category> results = categoryService.findAll();

            assertEquals(2, results.size());
        }
    }

    @Nested
    @DisplayName("Save Category Tests")
    class SaveCategoryTests {

        @Test
        @DisplayName("Should save new category and return it")
        void shouldSaveNewCategory() {
            Category newCategory = new Category("Photography", "Cameras and lenses");

            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            Category result = categoryService.save(newCategory);

            assertNotNull(result.getId());
            assertEquals("Photography", result.getName());
            verify(categoryRepository).save(newCategory);
        }
    }

    @Nested
    @DisplayName("Deletion Tests (BR-06)")
    class DeletionTests {

        @Test
        @DisplayName("Should delete category with no assigned equipment")
        void shouldDeleteCategoryWithNoEquipment() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(equipmentRepository.findByCategoryId(1L)).thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> categoryService.deleteById(1L));

            verify(categoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should block deletion when equipment is assigned to category")
        void shouldBlockDeletionWhenEquipmentAssigned() {
            Equipment assignedEquipment = new Equipment();
            assignedEquipment.setId(1L);
            assignedEquipment.setName("Wireless Microphone");
            assignedEquipment.setStatus(EquipmentStatus.AVAILABLE);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(equipmentRepository.findByCategoryId(1L)).thenReturn(List.of(assignedEquipment));

            DeletionBlockedException exception = assertThrows(
                    DeletionBlockedException.class,
                    () -> categoryService.deleteById(1L));

            assertTrue(exception.getMessage().contains("Audio Equipment"));
            verify(categoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void shouldThrowWhenDeletingNonExistentCategory() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.deleteById(999L));
        }
    }
}
