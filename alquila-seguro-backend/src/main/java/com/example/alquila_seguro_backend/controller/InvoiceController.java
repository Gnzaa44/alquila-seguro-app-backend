package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.CreateInvoiceRequest;
import com.example.alquila_seguro_backend.dto.InvoiceResponse;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.repositories.InvoiceRepository;
import com.example.alquila_seguro_backend.services.InvoiceService;
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
@RequestMapping("/alquila-seg/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }
    @GetMapping("/reservation/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByReservationId(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceByReservationId(id));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(@PathVariable DocumentStatus status) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStatus(status));
    }
    @PostMapping()
    public ResponseEntity <ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        try {
            return ResponseEntity.ok(invoiceService.createInvoice(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity <ApiResponse<InvoiceResponse>> updateInvoiceByStatus( @PathVariable Long id, @Valid DocumentStatus status) {
        try {
            return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
