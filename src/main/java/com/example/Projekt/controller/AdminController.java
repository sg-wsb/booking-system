package com.example.Projekt.controller;

import com.example.Projekt.model.Booking;
import com.example.Projekt.model.Slot;
import com.example.Projekt.repository.BookingRepository;
import com.example.Projekt.repository.SlotRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public AdminController(SlotRepository slotRepository,
                           BookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }


    @GetMapping
    public String adminDashboard(Model model) {

        long totalSlots = slotRepository.count();
        long totalBookings = bookingRepository.count();

        long bookedSlots = slotRepository.findAll()
                .stream()
                .filter(Slot::isBooked)
                .count();

        long freeSlots = totalSlots - bookedSlots;

        List<Booking> latestBookings = bookingRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();

        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("bookedSlots", bookedSlots);
        model.addAttribute("freeSlots", freeSlots);
        model.addAttribute("latestBookings", latestBookings);

        return "admin-dashboard";
    }


    @GetMapping("/slots")
    public String adminSlots(Model model) {
        model.addAttribute("slots", slotRepository.findAll());
        return "admin-slots";
    }


    @PostMapping("/slots")
    public String createSlot(@RequestParam String start,
                             @RequestParam String end,
                             RedirectAttributes redirectAttributes) {


        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Uzupełnij daty");
            return "redirect:/admin/slots";
        }

        LocalDateTime startTime;
        LocalDateTime endTime;

        try {
            startTime = LocalDateTime.parse(start);
            endTime = LocalDateTime.parse(end);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Niepoprawny format daty");
            return "redirect:/admin/slots";
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Nie można dodać terminu w przeszłości");
            return "redirect:/admin/slots";
        }

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            redirectAttributes.addFlashAttribute("error", "Koniec musi być po początku");
            return "redirect:/admin/slots";
        }

        Slot slot = new Slot();
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setBooked(false);

        slotRepository.save(slot);

        redirectAttributes.addFlashAttribute("success", "Termin dodany");

        return "redirect:/admin/slots";
    }


    @PostMapping("/slots/delete/{id}")
    public String deleteSlot(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {

        Slot slot = slotRepository.findById(id).orElseThrow();

        if (slot.isBooked()) {
            redirectAttributes.addFlashAttribute("error", "Nie można usunąć zajętego terminu");
            return "redirect:/admin/slots";
        }

        slotRepository.delete(slot);

        redirectAttributes.addFlashAttribute("success", "Termin usunięty");

        return "redirect:/admin/slots";
    }


    @GetMapping("/bookings")
    public String adminBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "admin-bookings";
    }


    @PostMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Booking booking = bookingRepository.findById(id).orElseThrow();

        Slot slot = booking.getSlot();
        slot.setBooked(false);

        slotRepository.save(slot);
        bookingRepository.delete(booking);

        redirectAttributes.addFlashAttribute("success", "Rezerwacja anulowana");

        return "redirect:/admin/bookings";
    }
    @PostMapping("/slots/generate")
    public String generateSlots(@RequestParam String date,
                                @RequestParam String startTime,
                                @RequestParam String endTime,
                                @RequestParam int interval,
                                RedirectAttributes redirectAttributes) {

        LocalDate localDate = LocalDate.parse(date);

        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {

            redirectAttributes.addFlashAttribute("error",
                    "Nie można tworzyć terminów w weekend");

            return "redirect:/admin/slots";
        }

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        LocalDateTime current = LocalDateTime.of(localDate, start);
        LocalDateTime endDateTime = LocalDateTime.of(localDate, end);

        int count = 0;

        while (!current.plusMinutes(interval).isAfter(endDateTime)) {

            Slot slot = new Slot();
            slot.setStartTime(current);
            slot.setEndTime(current.plusMinutes(interval));
            slot.setBooked(false);

            slotRepository.save(slot);

            current = current.plusMinutes(interval);
            count++;
        }

        redirectAttributes.addFlashAttribute("success",
                "Dodano " + count + " terminów");

        return "redirect:/admin/slots";
    }
}