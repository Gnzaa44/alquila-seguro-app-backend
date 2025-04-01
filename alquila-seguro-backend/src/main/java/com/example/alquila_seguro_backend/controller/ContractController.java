package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ContractResponse;
import com.example.alquila_seguro_backend.dto.CreateContractRequest;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alquila-seg/contracts")
@RequiredArgsConstructor
public class ContractController {

    private ContractService contractService;

    @GetMapping("/reservation/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractByReservationId(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractByReservationId(id));
    }
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByStatus(@PathVariable DocumentStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }
    @PostMapping()
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(@Valid @RequestBody CreateContractRequest request) {
        return ResponseEntity.ok(contractService.createContract(request));
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContractStatus(@PathVariable Long id, @Valid @RequestParam DocumentStatus status) {
        return ResponseEntity.ok(contractService.updateContractStatus(id, status));
    }
}

