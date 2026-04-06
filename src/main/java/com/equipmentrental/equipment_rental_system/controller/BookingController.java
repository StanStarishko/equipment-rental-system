package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.BookingConflictException;
import com.equipmentrental.equipment_rental_system.exception.EquipmentNotAvailableException;
import com.equipmentrental.equipment_rental_system.exception.InvalidBookingDatesException;
import com.equipmentrental.equipment_rental_system.model.Booking;
import com.equipmentrental.equipment_rental_system.service.BookingService;
import com.equipmentrental.equipment_rental_system.service.EquipmentService;
import com.equipmentrental.equipment_rental_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final UserService userService;

    public BookingController(BookingService bookingService,
                             EquipmentService equipmentService,
                             UserService userService) {
        this.bookingService = bookingService;
        this.equipmentService = equipmentService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "bookings");
        model.addAttribute("bookings", bookingService.findAll());
        return "booking/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("activePage", "bookings");
        model.addAttribute("booking", new Booking());
        model.addAttribute("equipmentList", equipmentService.findAll());
        model.addAttribute("users", userService.findAll());
        return "booking/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Booking booking,
                       @RequestParam Long equipmentId,
                       @RequestParam Long userId,
                       RedirectAttributes redirectAttributes) {
        try {
            booking.setEquipment(equipmentService.findById(equipmentId));
            booking.setUser(userService.findById(userId));
            bookingService.createBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully.");
            return "redirect:/bookings";
        } catch (InvalidBookingDatesException | EquipmentNotAvailableException | BookingConflictException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/bookings/new";
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public String cancel(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        bookingService.cancelBooking(bookingId);
        redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        return "redirect:/bookings";
    }
}
