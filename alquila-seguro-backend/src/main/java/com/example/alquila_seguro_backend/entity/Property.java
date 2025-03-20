package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
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

    @OneToMany(mappedBy = "property")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "property")
    private List<Consultancy> consultancies;

    @NotBlank(message = "Titulo obligatorio.")
    @Size(min = 3, max = 100, message = "El titulo debe contener entre 3 y 100 caracteres.")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Descripción obligatoria.")
    @Size(min = 10, max = 500, message = "La descripción debe contener entre 10 y 500 caracteres.")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "Localización obligatoria.")
    @Column(nullable = false)
    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0.")
    @Column(nullable = false)
    private Double pricePerNight;

    @NotBlank(message = "Categoria obligatoria.")
    @Size(min = 3, max = 50, message = "La categoria debe contener entre 3 y 50 caracteres.")
    @Column(nullable = false)
    private String category;

    @DecimalMin(value = "-90.0", inclusive = true, message = "Latitud no puede ser menor a 90.")
    @DecimalMax(value = "90.0", inclusive = true, message = "latitud no puede ser mayor que 90.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Longitud no puede menor que -180.")
    @DecimalMax(value = "180.0", inclusive = true, message = "Longitud no puede ser mayor que 180.")
    private BigDecimal longitude;

    @Min(value = 0, message = "El número de habitaciones debe ser mayor o igual que 0.")
    private int numberOfRooms;

    @Min(value = 0, message = "El número de baños debe ser mayor o igual que 0.")
    private int numberOfBathrooms;

    @DecimalMin(value = "0.0", inclusive = false, message = "El tamaño en m² debe ser mayor que 0.")
    private BigDecimal size;

    @NotEmpty(message = "Debe tener al menos una caracteristica.")
    @ElementCollection
    @CollectionTable(name = "property_features", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "feature")
    private Set<String> features = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    @URL
    @Column(nullable = false)
    private String imageUrl;

    @NotNull(message = "Estado de propiedad obligatorio.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyStatus propertyStatus;

    @NotNull(message = "Tipo de propiedad obligatorio.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;


}
