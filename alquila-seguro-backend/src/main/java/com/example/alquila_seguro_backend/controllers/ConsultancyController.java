package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.User;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultancies")
public class ConsultancyController {
    @Autowired
    private ConsultancyService consultancyService;
    @PostMapping("/add")
    public ResponseEntity<Consultancy> createConsultancy(@RequestBody Consultancy consultancy) {
        Consultancy newConsultancy = consultancyService.createConsultancy(consultancy);
        return ResponseEntity.ok(newConsultancy);
    }

    @GetMapping("/")
    public List<Consultancy> getAllConsultancies() {
        return consultancyService.getAllConsultancy();
    }

    @GetMapping("/user/{userId}")
    public List<Consultancy> getConsultanciesByUser(@PathVariable Long userId) {
        User user = new User(userId);
        return consultancyService.getConsultancyByUser(user);
    }
}
