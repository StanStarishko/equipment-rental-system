package com.equipmentrental.equipment_rental_system.service;

import com.equipmentrental.equipment_rental_system.exception.DeletionBlockedException;
import com.equipmentrental.equipment_rental_system.exception.ResourceNotFoundException;
import com.equipmentrental.equipment_rental_system.model.BookingStatus;
import com.equipmentrental.equipment_rental_system.model.User;
import com.equipmentrental.equipment_rental_system.repository.BookingRepository;
import com.equipmentrental.equipment_rental_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository,
                       BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", null));
    }

    @Transactional
    public User save(User user) {
        User savedUser = userRepository.save(user);
        log.info("User saved: '{}' (ID: {}, role: {})",
                savedUser.getFullName(), savedUser.getId(), savedUser.getRole());
        return savedUser;
    }

    @Transactional
    public void deleteById(Long userId) {
        User user = findById(userId);

        boolean hasActiveBookings = bookingRepository.findByUserId(userId).stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.CONFIRMED);

        if (hasActiveBookings) {
            log.warn("Deletion blocked for user '{}' (ID: {}): active bookings exist",
                    user.getFullName(), userId);
            throw new DeletionBlockedException(
                    "user '" + user.getFullName() + "'",
                    "this user has active bookings that must be cancelled first");
        }

        userRepository.delete(user);
        log.info("User deleted: '{}' (ID: {})", user.getFullName(), userId);
    }
}
