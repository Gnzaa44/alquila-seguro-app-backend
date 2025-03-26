package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyService {
    PropertyRepository propertyRepository;

    private PropertyResponse mapToPropertyResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .location(property.getLocation())
                .pricePerNight(property.getPricePerNight())
                .category(property.getCategory())
                .longitude(property.getLongitude())
                .latitude(property.getLatitude())
                .numberOfRooms(property.getNumberOfRooms())
                .numberOfBathrooms(property.getNumberOfBathrooms())
                .size(property.getSize())
                .features(property.getFeatures())
                .amenities(property.getAmenities())
                .imageUrl(property.getImageUrl())
                .propertyStatus(property.getPropertyStatus())
                .propertyType(property.getPropertyType())
                .build();

    }
    
}
