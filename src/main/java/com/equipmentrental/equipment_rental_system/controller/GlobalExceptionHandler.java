package com.equipmentrental.equipment_rental_system.controller;

import com.equipmentrental.equipment_rental_system.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Centralised exception handler for all controllers in the application.
 * Uses Spring's {@link ControllerAdvice} to intercept exceptions thrown by any controller
 * and route them to the error page with an appropriate status code and user-friendly message.
 *
 * <p>Each custom exception type has a dedicated handler method. A catch-all handler
 * covers unexpected exceptions to ensure users never see a raw stack trace.</p>
 *
 * @see ResourceNotFoundException
 * @see BookingConflictException
 * @see EquipmentNotAvailableException
 * @see InvalidBookingDatesException
 * @see DeletionBlockedException
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles cases where a requested entity was not found by its ID.
     *
     * @param exception the exception containing the resource name and ID
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException exception, Model model) {
        log.warn("Resource not found: {}", exception.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    /**
     * Handles booking date conflicts (BR-02).
     *
     * @param exception the exception describing the conflict
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
    @ExceptionHandler(BookingConflictException.class)
    public String handleBookingConflict(BookingConflictException exception, Model model) {
        log.warn("Booking conflict: {}", exception.getMessage());
        model.addAttribute("status", 409);
        model.addAttribute("error", "Booking Conflict");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    /**
     * Handles attempts to book equipment that is not available (BR-01).
     *
     * @param exception the exception describing the unavailable equipment
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
    @ExceptionHandler(EquipmentNotAvailableException.class)
    public String handleEquipmentNotAvailable(EquipmentNotAvailableException exception, Model model) {
        log.warn("Equipment not available: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Equipment Not Available");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    /**
     * Handles invalid booking date ranges (BR-05).
     *
     * @param exception the exception describing the invalid dates
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
    @ExceptionHandler(InvalidBookingDatesException.class)
    public String handleInvalidBookingDates(InvalidBookingDatesException exception, Model model) {
        log.warn("Invalid booking dates: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Invalid Dates");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    /**
     * Handles attempts to delete entities with dependent records (BR-06, BR-07).
     *
     * @param exception the exception explaining why deletion was blocked
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
    @ExceptionHandler(DeletionBlockedException.class)
    public String handleDeletionBlocked(DeletionBlockedException exception, Model model) {
        log.warn("Deletion blocked: {}", exception.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Cannot Delete");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    /**
     * Catches any unhandled exception, including missing static resources (favicon.ico).
     * Logs the full stack trace for unexpected errors and returns a generic error page.
     *
     * @param exception the unhandled exception
     * @param model     the model to populate with error attributes
     * @return the error view name
     */
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
