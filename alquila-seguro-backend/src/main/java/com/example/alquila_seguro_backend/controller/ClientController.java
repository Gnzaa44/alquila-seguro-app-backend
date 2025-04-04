package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ReservationRepository;
import com.example.alquila_seguro_backend.services.ClientService;
import com.example.alquila_seguro_backend.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alquila-seg/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }
    @PostMapping()
    public ResponseEntity<ApiResponse<ClientResponse>> createClient( @Valid @RequestBody ClientCreateRequest request) {
        return ResponseEntity.ok(clientService.createClient(request));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(@PathVariable Long id, @Valid @RequestBody ClientCreateRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.deleteClient(id));
    }

}
