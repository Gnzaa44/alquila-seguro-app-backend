package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.entity.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/properties")
public class PropertyController {
    @Autowired
    private PropertyService propertyService;

    @PostMapping("/add")
    public ResponseEntity<Property> addProperty(@RequestBody Property property) {
        Property newProperty = propertyService.addProperty(property);
        return ResponseEntity.ok(newProperty);
    }
    @GetMapping("/available")
    public List<Property> getAvailableProperties() {
        return propertyService.getAvailableProperties();
    }
    @GetMapping("/search")
    public List<Property> searchProperties(@RequestParam String location) {
        return propertyService.searchProperties(location);
    }

}
