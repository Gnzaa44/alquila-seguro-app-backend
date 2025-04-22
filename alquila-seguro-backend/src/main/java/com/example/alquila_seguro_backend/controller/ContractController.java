package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ContractResponse;
import com.example.alquila_seguro_backend.dto.CreateContractRequest;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.services.ContractService;
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
@RequestMapping("/alquila-seg/contracts")
@RequiredArgsConstructor
public class ContractController {

    private ContractService contractService;

    @GetMapping("/reservation/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractByReservationId(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractByReservationId(id));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByStatus(@PathVariable DocumentStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }
    @PostMapping()
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(@Valid @RequestBody CreateContractRequest request) {
        try {
            return ResponseEntity.ok(contractService.createContract(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContractStatus(@PathVariable Long id, @Valid @RequestParam DocumentStatus status) {
        try {
            return ResponseEntity.ok(contractService.updateContractStatus(id, status));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

