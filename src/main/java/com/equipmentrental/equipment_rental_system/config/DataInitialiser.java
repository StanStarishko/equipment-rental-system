package com.equipmentrental.equipment_rental_system.config;

import com.equipmentrental.equipment_rental_system.model.*;
import com.equipmentrental.equipment_rental_system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Populates the database with sample data on application startup.
 * Implements {@link CommandLineRunner} so it executes automatically after
 * the Spring context is initialised.
 *
 * <p>Creates a representative set of categories, users, equipment items and
 * bookings to demonstrate the system's functionality without requiring
 * manual data entry. All dates use relative offsets from the current date
 * so the sample data remains relevant regardless of when the application is started.</p>
 */
@Component
public class DataInitialiser implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitialiser.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;

    public DataInitialiser(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           EquipmentRepository equipmentRepository,
                           BookingRepository bookingRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initialising sample data...");

        // Categories
        Category audioCategory = categoryRepository.save(
                new Category("Audio Equipment", "Microphones, speakers, mixers and audio accessories"));
        Category computingCategory = categoryRepository.save(
                new Category("Computing", "Laptops, tablets and desktop workstations"));
        Category photographyCategory = categoryRepository.save(
                new Category("Photography", "Cameras, lenses, tripods and lighting kits"));

        // Users
        User adminUser = userRepository.save(new User(
                "admin", "admin123", "Helen", "Murray",
                UserRole.ADMIN, "Administration", "0141 552 1001", "h.murray@organisation.co.uk"));
        User staffMember1 = userRepository.save(new User(
                "jthompson", "staff123", "James", "Thompson",
                UserRole.STAFF, "Events", "0141 552 1002", "j.thompson@organisation.co.uk"));
        User staffMember2 = userRepository.save(new User(
                "skaur", "staff123", "Simran", "Kaur",
                UserRole.STAFF, "Training", "0141 552 1003", "s.kaur@organisation.co.uk"));

        // Equipment
        Equipment wirelessMic = equipmentRepository.save(new Equipment(
                "Shure SM58 Wireless Microphone", "Dynamic vocal microphone with wireless receiver",
                "Storage Room A", "Good", EquipmentStatus.AVAILABLE,
                LocalDate.of(2024, 3, 15), new BigDecimal("12.00"), audioCategory));

        Equipment mixer = equipmentRepository.save(new Equipment(
                "Yamaha MG12 Mixing Console", "12-channel analogue mixer",
                "Storage Room A", "Good", EquipmentStatus.AVAILABLE,
                LocalDate.of(2023, 9, 1), new BigDecimal("25.00"), audioCategory));

        Equipment laptopDell = equipmentRepository.save(new Equipment(
                "Dell Latitude 5540 Laptop", "15-inch business laptop, 16GB RAM, 512GB SSD",
                "IT Office", "Good", EquipmentStatus.AVAILABLE,
                LocalDate.of(2024, 6, 10), new BigDecimal("18.00"), computingCategory));

        Equipment laptopLenovo = equipmentRepository.save(new Equipment(
                "Lenovo ThinkPad T14 Laptop", "14-inch ultrabook, 16GB RAM, 256GB SSD",
                "IT Office", "Fair", EquipmentStatus.MAINTENANCE,
                LocalDate.of(2023, 1, 20), new BigDecimal("15.00"), computingCategory));

        Equipment canonCamera = equipmentRepository.save(new Equipment(
                "Canon EOS R6 Camera", "Full-frame mirrorless camera body",
                "Storage Room B", "Excellent", EquipmentStatus.AVAILABLE,
                LocalDate.of(2024, 11, 5), new BigDecimal("35.00"), photographyCategory));

        Equipment tripod = equipmentRepository.save(new Equipment(
                "Manfrotto 055 Tripod", "Aluminium tripod with ball head",
                "Storage Room B", "Good", EquipmentStatus.AVAILABLE,
                LocalDate.of(2022, 7, 12), new BigDecimal("8.00"), photographyCategory));

        // Bookings
        bookingRepository.save(new Booking(
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(5),
                "Staff training session audio setup",
                BookingStatus.CONFIRMED, wirelessMic, staffMember1));

        bookingRepository.save(new Booking(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "Photography for annual report",
                BookingStatus.CONFIRMED, canonCamera, staffMember2));

        bookingRepository.save(new Booking(
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(7),
                "Community event presentation",
                BookingStatus.CONFIRMED, laptopDell, staffMember1));

        bookingRepository.save(new Booking(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(5),
                "Workshop recording",
                BookingStatus.CANCELLED, mixer, staffMember2));

        log.info("Sample data initialised: {} categories, {} users, {} equipment items, {} bookings",
                categoryRepository.count(), userRepository.count(),
                equipmentRepository.count(), bookingRepository.count());
    }
}
