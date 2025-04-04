package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ReservationCreateRequest;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.ReservationStatus;
import com.example.alquila_seguro_backend.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alquila-seg/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getReservationsByClientId(clientId));
    }
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByPropertyId(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reservationService.getReservationsByPropertyId(propertyId));
    }
    @GetMapping("/status/{statusId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByStatusId(@PathVariable ReservationStatus statusId) {
        return ResponseEntity.ok(reservationService.getReservationsByStatus(statusId));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }
    @PutMapping("{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ReservationResponse>> completeReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.completeReservation(id));
    }



}
