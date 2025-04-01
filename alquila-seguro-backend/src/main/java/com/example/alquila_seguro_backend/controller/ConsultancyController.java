package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientCreateRequest;
import com.example.alquila_seguro_backend.dto.ConsultancyCreateRequest;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alquila-seg/consultancies")
@RequiredArgsConstructor
public class ConsultancyController {
    private final ConsultancyService consultancyService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConsultancyResponse>> createConsultancy(@Valid @RequestBody ConsultancyCreateRequest request) {
        return ResponseEntity.ok(consultancyService.createConsultancy(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> updateConsultancyStatus(
            @PathVariable Long id,
            @Valid @RequestParam ConsultancyStatus status) {
        return ResponseEntity.ok(consultancyService.updateConsultancyStatus(id, status));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByClient(clientId));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByProperty(propertyId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByStatus(@PathVariable ConsultancyStatus status) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByStatus(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> getConsultancyById(@PathVariable Long id) {
        return ResponseEntity.ok(consultancyService.getConsultancyById(id));
    }
}
