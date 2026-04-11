package com.example.Projekt.controller;

import com.example.Projekt.model.Booking;
import com.example.Projekt.model.Slot;
import com.example.Projekt.repository.BookingRepository;
import com.example.Projekt.repository.SlotRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    public BookingController(BookingRepository bookingRepository,
                             SlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }


    @GetMapping("/my-bookings")
    public String myBookings(Model model,
                             Authentication auth) {

        String email = auth.getName();

        List<Booking> bookings = bookingRepository.findAll()
                .stream()
                .filter(b -> b.getUser().getEmail().equals(email))
                .toList();

        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }


    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {

        Booking booking = bookingRepository.findById(id).orElseThrow();


        if (!booking.getUser().getEmail().equals(auth.getName())) {
            redirectAttributes.addFlashAttribute("error", "Brak dostępu");
            return "redirect:/my-bookings";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = booking.getSlot().getStartTime();


        if (start.minusHours(24).isBefore(now)) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie można anulować mniej niż 24h przed wizytą");
            return "redirect:/my-bookings";
        }


        Slot slot = booking.getSlot();
        slot.setBooked(false);
        slotRepository.save(slot);


        bookingRepository.delete(booking);

        redirectAttributes.addFlashAttribute("success", "Rezerwacja anulowana");

        return "redirect:/my-bookings";
    }
}