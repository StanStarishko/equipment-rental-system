package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.model.Equipment;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import com.equipmentrental.equipment_rental_system.service.CategoryService;
import com.equipmentrental.equipment_rental_system.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final CategoryService categoryService;

    public EquipmentController(EquipmentService equipmentService,
                               CategoryService categoryService) {
        this.equipmentService = equipmentService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) EquipmentStatus status,
                       Model model) {
        model.addAttribute("activePage", "equipment");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", EquipmentStatus.values());

        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentStatus", status);

        List<Equipment> equipment;
        if (search != null && !search.isBlank()) {
            equipment = equipmentService.searchByName(search);
        } else if (categoryId != null) {
            equipment = equipmentService.findByCategoryId(categoryId);
        } else if (status != null) {
            equipment = equipmentService.findByStatus(status);
        } else {
            equipment = equipmentService.findAll();
        }

        model.addAttribute("equipmentList", equipment);
        return "equipment/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("activePage", "equipment");
        model.addAttribute("equipment", new Equipment());
        model.addAttribute("formTitle", "Add Equipment");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", EquipmentStatus.values());
        return "equipment/form";
    }

    @GetMapping("/{equipmentId}/edit")
    public String showEditForm(@PathVariable Long equipmentId, Model model) {
        model.addAttribute("activePage", "equipment");
        model.addAttribute("equipment", equipmentService.findById(equipmentId));
        model.addAttribute("formTitle", "Edit Equipment");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", EquipmentStatus.values());
        return "equipment/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Equipment equipment,
                       BindingResult bindingResult,
                       @RequestParam Long categoryId,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "equipment");
            model.addAttribute("formTitle", equipment.getId() == null ? "Add Equipment" : "Edit Equipment");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("statuses", EquipmentStatus.values());
            return "equipment/form";
        }
        equipment.setCategory(categoryService.findById(categoryId));
        equipmentService.save(equipment);
        redirectAttributes.addFlashAttribute("successMessage", "Equipment saved successfully.");
        return "redirect:/equipment";
    }

    @PostMapping("/{equipmentId}/delete")
    public String delete(@PathVariable Long equipmentId, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.deleteById(equipmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Equipment deleted successfully.");
        } catch (DeletionBlockedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/equipment";
    }
}
