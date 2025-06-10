package com.example.alquila_seguro_backend.dto;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price per night must be greater than 0")
    private Double pricePerNight;

    @NotBlank(message = "Category is required")
    @Size(min = 3, max = 50, message = "Category must be between 3 and 50 characters")
    private String category;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Min(value = 0, message = "Number of rooms must be greater than or equal to 0")
    private int numberOfRooms;

    @Min(value = 0, message = "Number of bathrooms must be greater than or equal to 0")
    private int numberOfBathrooms;

    @DecimalMin(value = "0.0", inclusive = false, message = "Size must be greater than 0")
    private BigDecimal size;

    @NotEmpty(message = "Must have at least one feature")
    private Set<String> features;

    private Set<String> amenities;

    @URL
    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyStatus propertyStatus;

}

