package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.model.BookingStatus;
import com.equipmentrental.equipment_rental_system.model.EquipmentStatus;
import com.equipmentrental.equipment_rental_system.service.BookingService;
import com.equipmentrental.equipment_rental_system.service.CategoryService;
import com.equipmentrental.equipment_rental_system.service.EquipmentService;
import com.equipmentrental.equipment_rental_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final CategoryService categoryService;
    private final UserService userService;

    public DashboardController(EquipmentService equipmentService,
                               BookingService bookingService,
                               CategoryService categoryService,
                               UserService userService) {
        this.equipmentService = equipmentService;
        this.bookingService = bookingService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");

        // Equipment stats
        long totalEquipment = equipmentService.findAll().size();
        long availableCount = equipmentService.findByStatus(EquipmentStatus.AVAILABLE).size();
        long bookedCount = equipmentService.findByStatus(EquipmentStatus.BOOKED).size();
        long maintenanceCount = equipmentService.findByStatus(EquipmentStatus.MAINTENANCE).size();

        model.addAttribute("totalEquipment", totalEquipment);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("bookedCount", bookedCount);
        model.addAttribute("maintenanceCount", maintenanceCount);

        // Other stats
        model.addAttribute("totalCategories", categoryService.findAll().size());
        model.addAttribute("totalUsers", userService.findAll().size());

        // Recent and upcoming bookings
        var allBookings = bookingService.findAll();
        var activeBookings = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("totalBookings", allBookings.size());

        return "dashboard";
    }
}
