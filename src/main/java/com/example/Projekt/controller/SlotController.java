package com.example.Projekt.controller;

import com.example.Projekt.model.Slot;
import com.example.Projekt.model.User;
import com.example.Projekt.repository.SlotRepository;
import com.example.Projekt.repository.UserRepository;
import com.example.Projekt.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SlotController {

    private final SlotRepository slotRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public SlotController(SlotRepository slotRepository,
                          BookingService bookingService,
                          UserRepository userRepository) {

        this.slotRepository = slotRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/slots")
    public String listSlots(@RequestParam(required = false) String date,
                            @RequestParam(required = false) Boolean onlyFree,
                            Model model,
                            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin";
        }

        List<Slot> slots = slotRepository.findAll();


        if (onlyFree != null && onlyFree) {
            slots = slots.stream()
                    .filter(s -> !s.isBooked())
                    .toList();
        }


        if (date != null && !date.isBlank()) {
            LocalDate selectedDate = LocalDate.parse(date);

            slots = slots.stream()
                    .filter(s -> s.getStartTime().toLocalDate().equals(selectedDate))
                    .toList();
        }

        model.addAttribute("slots", slots);

        return "slots";
    }

    @PostMapping("/book/{id}")
    public String bookSlot(@PathVariable Long id,
                           Authentication authentication) {

        String email = authentication.getName();
        bookingService.bookSlot(id, email);

        return "redirect:/slots";
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "slots-calendar";
    }
}