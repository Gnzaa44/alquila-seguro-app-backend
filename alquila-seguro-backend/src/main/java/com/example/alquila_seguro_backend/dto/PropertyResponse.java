package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.entity.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double pricePerNight;
    private String category;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int numberOfRooms;
    private int numberOfBathrooms;
    private BigDecimal size;
    private Set<String> features;
    private Set<String> amenities;
    private String imageUrl;
    private PropertyType propertyType;
    private PropertyStatus propertyStatus;
}
