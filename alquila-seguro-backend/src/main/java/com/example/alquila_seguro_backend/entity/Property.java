package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "title is required")
    @Size(min = 3, max = 100, message = "title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "description is required")
    @Size(min = 10, max = 500, message = "description must be between 10 and 500 characters")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "location is required")
    @Column(nullable = false)
    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
    @Column(nullable = false)
    private Double pricePerNight;

    @NotBlank(message = "category is required")
    @Size(min = 3, max = 50, message = "category must be between 3 and 50 characters")
    @Column(nullable = false)
    private String category;

    @DecimalMin(value = "-90.0", inclusive = true, message = "latitude cannot be less than 90")
    @DecimalMax(value = "90.0", inclusive = true, message = "latitude cannot be greater than 90")
    private double latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "longitude cannot be less than -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "longitude cannot be greater than 180")
    private double longitude;

    @NotEmpty(message = "must have at least one feature")
    @ElementCollection
    @CollectionTable(name = "property_features", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "feature")
    private Set<String> features = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private boolean available;

    @OneToMany(mappedBy = "property")
    private List<Reservation> reservations;

}
