package com.example.Projekt.exception;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public String handleSlotAlreadyBooked(SlotAlreadyBookedException ex,
                                          Model model) {

        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }
}
