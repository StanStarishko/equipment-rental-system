package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.model.User;
import com.equipmentrental.equipment_rental_system.model.UserRole;
import com.equipmentrental.equipment_rental_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "users");
        model.addAttribute("users", userService.findAll());
        return "user/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("activePage", "users");
        model.addAttribute("user", new User());
        model.addAttribute("formTitle", "Add User");
        model.addAttribute("roles", UserRole.values());
        return "user/form";
    }

    @GetMapping("/{userId}/edit")
    public String showEditForm(@PathVariable Long userId, Model model) {
        model.addAttribute("activePage", "users");
        model.addAttribute("user", userService.findById(userId));
        model.addAttribute("formTitle", "Edit User");
        model.addAttribute("roles", UserRole.values());
        return "user/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute User user,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "users");
            model.addAttribute("formTitle", user.getId() == null ? "Add User" : "Edit User");
            model.addAttribute("roles", UserRole.values());
            return "user/form";
        }
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "User saved successfully.");
        return "redirect:/users";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (DeletionBlockedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/users";
    }
}
