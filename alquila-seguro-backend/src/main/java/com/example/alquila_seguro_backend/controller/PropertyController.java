package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.PropertyCreateRequest;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.services.PropertyService;
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
@RequestMapping("alquila-seg/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAvailableProperties() {
        return ResponseEntity.ok(propertyService.getAvailableProperties());
    }
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(propertyService.getPropertiesByCategory(category));
    }
    @GetMapping("/max-price/{price}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByMaxPrice(@PathVariable Double price) {
        return ResponseEntity.ok(propertyService.getPropertiesByMaxPrice(price));
    }
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByLocation(@PathVariable String location) {
        return ResponseEntity.ok(propertyService.getPropertiesByLocation(location));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(@Valid @RequestBody PropertyCreateRequest request){
        return ResponseEntity.ok(propertyService.createProperty(request));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity <ApiResponse<PropertyResponse>> updateProperty(@Valid @RequestBody PropertyCreateRequest request, @PathVariable Long id){
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable Long id){
        return ResponseEntity.ok(propertyService.deleteProperty(id));
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updatePropertyByStatus(@PathVariable Long id, @Valid @RequestParam PropertyStatus status){
        return ResponseEntity.ok(propertyService.updatePropertyByStatus(id, status));
    }


}
