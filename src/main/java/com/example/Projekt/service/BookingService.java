package com.example.Projekt.service;

import com.example.Projekt.model.Booking;
import com.example.Projekt.model.Slot;
import com.example.Projekt.model.User;
import com.example.Projekt.repository.BookingRepository;
import com.example.Projekt.repository.SlotRepository;
import com.example.Projekt.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          SlotRepository slotRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void bookSlot(Long slotId, String userEmail) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow();

        if (slot.isBooked()) {
            throw new RuntimeException("Slot already booked");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow();

        slot.setBooked(true);

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setUser(user);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, String userEmail) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        // pozwalamy anulować tylko swoje rezerwacje
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You cannot cancel this booking");
        }

        Slot slot = booking.getSlot();
        slot.setBooked(false);

        bookingRepository.delete(booking);
    }
}