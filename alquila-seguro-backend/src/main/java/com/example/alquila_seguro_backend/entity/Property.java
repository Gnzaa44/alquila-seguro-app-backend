package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "title is required")
    @Size(min = 3, max = 100, message = "title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "description is required")
    @Size(min = 10, max = 500, message = "description must be between 10 and 500 characters")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "location is required")
    @Column(nullable = false)
    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
    @Column(nullable = false)
    private Double pricePerNight;

    @NotNull(message = "category is required")
    @Size(min = 3, max = 50, message = "category must be between 3 and 50 characters")
    @Column(nullable = false)
    private String category;

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
