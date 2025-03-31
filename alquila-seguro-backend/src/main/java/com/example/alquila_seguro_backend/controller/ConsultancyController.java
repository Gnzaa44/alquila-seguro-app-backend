package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.services.ConsultancyService;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> createConsultancy(
            @RequestParam Long clientId,
            @RequestParam Long propertyId,
            @RequestParam String details) {
        return ResponseEntity.ok(consultancyService.createConsultancy(clientId, propertyId, details));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")  // Solo admins pueden cambiar el estado de una consultoría
    public ResponseEntity<ApiResponse<ConsultancyResponse>> updateConsultancyStatus(
            @PathVariable Long id,
            @RequestParam ConsultancyStatus status) {
        return ResponseEntity.ok(consultancyService.updateConsultancyStatus(id, status));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")  // Clientes solo ven sus propias consultas
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByClient(clientId));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")  // Solo admins pueden ver consultas por propiedad
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByProperty(propertyId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")  // Solo admins pueden ver todas las consultas por estado
    public ResponseEntity<ApiResponse<List<ConsultancyResponse>>> getConsultanciesByStatus(@PathVariable ConsultancyStatus status) {
        return ResponseEntity.ok(consultancyService.getConsultanciesByStatus(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConsultancyResponse>> getConsultancyById(@PathVariable Long id) {
        return ResponseEntity.ok(consultancyService.getConsultancyById(id));
    }
}
