package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyCreateRequest;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/alquila-seg/consultancies")
@RequiredArgsConstructor
public class ConsultancyController {
    private final ConsultancyService consultancyService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConsultancyResponse>> createConsultancy(@Valid @RequestBody ConsultancyCreateRequest request) {
        try {
            return ResponseEntity.ok(consultancyService.createConsultancy(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> updateConsultancyStatus(
            @PathVariable Long id,
            @Valid @RequestParam ConsultancyStatus status) {
        try {
            return ResponseEntity.ok(consultancyService.updateConsultancyStatus(id, status));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultoria no encontrada.");
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByClient(clientId));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByProperty(propertyId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByStatus(@PathVariable ConsultancyStatus status) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> getConsultancyById(@PathVariable Long id) {
        return ResponseEntity.ok(consultancyService.getConsultancyById(id));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getAllConsultancies() {
        return ResponseEntity.ok(consultancyService.getAllConsultancies());
    }
}
