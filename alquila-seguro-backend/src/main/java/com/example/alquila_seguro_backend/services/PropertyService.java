package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {
    @Autowired
    private PropertyRepository propertyRepository;

    public Property addProperty(Property property) {
        return propertyRepository.save(property);
    }
    public List<Property> getAvailableProperties() {
        return propertyRepository.findByAvailability(true);
    }
    public List<Property> searchProperties(String location) {
        return propertyRepository.findByLocation(location);
    }
}
