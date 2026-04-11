package com.example.Projekt.controller;

import com.example.Projekt.model.Ticket;
import com.example.Projekt.repository.TicketRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/contact")
    public String contactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String sendTicket(@RequestParam String subject,
                             @RequestParam String message,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        Ticket t = new Ticket();

        t.setEmail(auth.getName());
        t.setSubject(subject);
        t.setMessage("USER:\n" + message);
        t.setStatus("NEW");
        t.setCreatedAt(LocalDateTime.now());

        ticketRepository.save(t);

        redirectAttributes.addFlashAttribute("success", "Wysłano wiadomość");

        return "redirect:/contact";
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model, Authentication auth) {

        String email = auth.getName();

        List<Ticket> tickets = ticketRepository.findAll()
                .stream()
                .filter(t -> t.getEmail().equals(email))
                .toList();

        model.addAttribute("tickets", tickets);

        return "my-tickets";
    }

    @PostMapping("/tickets/reply/{id}")
    public String userReply(@PathVariable Long id,
                            @RequestParam String message,
                            RedirectAttributes redirectAttributes) {

        Ticket t = ticketRepository.findById(id).orElseThrow();

        t.setMessage(t.getMessage() + "\n\nUSER:\n" + message);

        ticketRepository.save(t);

        redirectAttributes.addFlashAttribute("success", "Odpowiedź wysłana");

        return "redirect:/my-tickets";
    }

    @GetMapping("/admin/tickets")
    public String adminTickets(Model model) {

        model.addAttribute("tickets", ticketRepository.findAll());

        return "admin-tickets";
    }

    @PostMapping("/admin/tickets/reply/{id}")
    public String adminReply(@PathVariable Long id,
                             @RequestParam String response,
                             RedirectAttributes redirectAttributes) {

        Ticket t = ticketRepository.findById(id).orElseThrow();

        t.setMessage(t.getMessage() + "\n\nADMIN:\n" + response);

        ticketRepository.save(t);

        redirectAttributes.addFlashAttribute("success", "Odpowiedź wysłana");

        return "redirect:/admin/tickets";
    }

    @PostMapping("/admin/tickets/close/{id}")
    public String close(@PathVariable Long id,
                        RedirectAttributes redirectAttributes) {

        Ticket t = ticketRepository.findById(id).orElseThrow();

        t.setStatus("CLOSED");

        ticketRepository.save(t);

        redirectAttributes.addFlashAttribute("success", "Ticket zamknięty");

        return "redirect:/admin/tickets";
    }
}