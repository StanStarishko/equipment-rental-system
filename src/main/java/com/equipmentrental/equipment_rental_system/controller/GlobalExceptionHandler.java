package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException exception, Model model) {
        log.warn("Resource not found: {}", exception.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(BookingConflictException.class)
    public String handleBookingConflict(BookingConflictException exception, Model model) {
        log.warn("Booking conflict: {}", exception.getMessage());
        model.addAttribute("status", 409);
        model.addAttribute("error", "Booking Conflict");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(EquipmentNotAvailableException.class)
    public String handleEquipmentNotAvailable(EquipmentNotAvailableException exception, Model model) {
        log.warn("Equipment not available: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Equipment Not Available");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(InvalidBookingDatesException.class)
    public String handleInvalidBookingDates(InvalidBookingDatesException exception, Model model) {
        log.warn("Invalid booking dates: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Invalid Dates");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(DeletionBlockedException.class)
    public String handleDeletionBlocked(DeletionBlockedException exception, Model model) {
        log.warn("Deletion blocked: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Cannot Delete");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception exception, Model model) {
        if (exception instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            model.addAttribute("status", 404);
            model.addAttribute("error", "Not Found");
            model.addAttribute("message", "The requested page was not found.");
            return "error";
        }
        log.error("Unexpected error occurred", exception);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred. Please try again.");
        return "error";
    }
}
