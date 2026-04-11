package com.example.Projekt.controller;

import com.example.Projekt.model.Slot;
import com.example.Projekt.repository.SlotRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SlotRepository slotRepository;

    public ApiController(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @GetMapping("/slots")
    public List<Map<String, Object>> getAllSlots() {

        return slotRepository.findAll().stream().map(slot -> {
            Map<String, Object> event = new HashMap<>();

            event.put("id", slot.getId());
            event.put("title", slot.isBooked() ? "Zajęty" : "Dostępny");
            event.put("start", slot.getStartTime().toString());
            event.put("end", slot.getEndTime().toString());


            event.put("color", slot.isBooked() ? "#dc3545" : "#198754");


            event.put("booked", slot.isBooked());

            return event;
        }).toList();
    }
}