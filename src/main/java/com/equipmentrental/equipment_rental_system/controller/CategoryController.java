package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.model.Category;
import com.equipmentrental.equipment_rental_system.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "categories");
        model.addAttribute("categories", categoryService.findAll());
        return "category/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("activePage", "categories");
        model.addAttribute("category", new Category());
        model.addAttribute("formTitle", "Add Category");
        return "category/form";
    }

    @GetMapping("/{categoryId}/edit")
    public String showEditForm(@PathVariable Long categoryId, Model model) {
        model.addAttribute("activePage", "categories");
        model.addAttribute("category", categoryService.findById(categoryId));
        model.addAttribute("formTitle", "Edit Category");
        return "category/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Category category,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "categories");
            model.addAttribute("formTitle", category.getId() == null ? "Add Category" : "Edit Category");
            return "category/form";
        }
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully.");
        return "redirect:/categories";
    }

    @PostMapping("/{categoryId}/delete")
    public String delete(@PathVariable Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (DeletionBlockedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/categories";
    }
}
