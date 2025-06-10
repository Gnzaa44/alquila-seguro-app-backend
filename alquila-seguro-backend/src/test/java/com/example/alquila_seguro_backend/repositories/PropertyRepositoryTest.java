package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Usa una DB en memoria para los tests
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    private Property property1;
    private Property property2;
    private Property property3;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll(); // Limpia la base de datos antes de cada test

        property1 = Property.builder()
                .title("Casa en la Playa")
                .description("Hermosa casa con vista al mar.")
                .location("Mar del Plata")
                .pricePerNight(150.0)
                .category("Casa")
                .latitude(new BigDecimal("-38.0055"))
                .longitude(new BigDecimal("-57.5426"))
                .numberOfRooms(3)
                .numberOfBathrooms(2)
                .size(new BigDecimal("120.5"))
                .features(new HashSet<>(Arrays.asList("Pileta", "Jardín")))
                .amenities(new HashSet<>(Arrays.asList("Wifi", "Aire Acondicionado")))
                .imageUrl("http://example.com/casa1.jpg")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();

        property2 = Property.builder()
                .title("Departamento Céntrico")
                .description("Departamento moderno en el corazón de la ciudad.")
                .location("Buenos Aires")
                .pricePerNight(80.0)
                .category("Departamento")
                .latitude(new BigDecimal("-34.6037"))
                .longitude(new BigDecimal("-58.3816"))
                .numberOfRooms(1)
                .numberOfBathrooms(1)
                .size(new BigDecimal("45.0"))
                .features(new HashSet<>(Arrays.asList("Balcón")))
                .amenities(new HashSet<>(Arrays.asList("Calefacción")))
                .imageUrl("http://example.com/depto1.jpg")
                .propertyStatus(PropertyStatus.RESERVED)
                .build();

        property3 = Property.builder()
                .title("Cabaña en la Montaña")
                .description("Acogedora cabaña con vistas a la sierra.")
                .location("Cordoba")
                .pricePerNight(120.0)
                .category("Cabaña")
                .latitude(new BigDecimal("-31.4167"))
                .longitude(new BigDecimal("-64.1833"))
                .numberOfRooms(2)
                .numberOfBathrooms(1)
                .size(new BigDecimal("70.0"))
                .features(new HashSet<>(Arrays.asList("Chimenea")))
                .amenities(new HashSet<>(Arrays.asList("Estacionamiento")))
                .imageUrl("http://example.com/cabana1.jpg")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();

        propertyRepository.saveAll(Arrays.asList(property1, property2, property3));
    }

    @Test
    @DisplayName("Debe encontrar propiedades por estado de propiedad")
    void findByPropertyStatus_shouldReturnPropertiesWithGivenStatus() {
        // Cuando
        List<Property> availableProperties = propertyRepository.findByPropertyStatus(PropertyStatus.AVAILABLE);

        // Entonces
        assertThat(availableProperties).hasSize(2);
        assertThat(availableProperties).containsExactlyInAnyOrder(property1, property3);
    }

    @Test
    @DisplayName("Debe encontrar propiedades por categoría")
    void findByCategory_shouldReturnPropertiesWithGivenCategory() {
        // Cuando
        List<Property> houses = propertyRepository.findByCategory("Casa");

        // Entonces
        assertThat(houses).hasSize(1);
        assertThat(houses).containsExactly(property1);
    }

    @Test
    @DisplayName("Debe encontrar propiedades por precio por noche menor o igual a un valor dado")
    void findByPricePerNightLessThanEqual_shouldReturnPropertiesBelowOrEqualMaxPrice() {
        // Cuando
        List<Property> cheapProperties = propertyRepository.findByPricePerNightLessThanEqual(100.0);

        // Entonces
        assertThat(cheapProperties).hasSize(1);
        assertThat(cheapProperties).containsExactly(property2);
    }

    @Test
    @DisplayName("Debe encontrar propiedades por ubicación conteniendo la cadena dada")
    void findByLocationContaining_shouldReturnPropertiesContainingLocationString() {
        // Cuando
        List<Property> propertiesInBuenos = propertyRepository.findByLocationContaining("Buenos");

        // Entonces
        assertThat(propertiesInBuenos).hasSize(1);
        assertThat(propertiesInBuenos).containsExactly(property2);
    }

    @Test
    @DisplayName("Debe encontrar una propiedad por ID existente")
    void findById_shouldReturnPropertyWhenIdExists() {
        // Cuando
        Optional<Property> foundProperty = propertyRepository.findById(property1.getId());

        // Entonces
        assertThat(foundProperty).isPresent();
        assertThat(foundProperty.get().getTitle()).isEqualTo("Casa en la Playa");
    }

    @Test
    @DisplayName("Debe retornar vacío cuando el ID no existe")
    void findById_shouldReturnEmptyWhenIdDoesNotExist() {
        // Cuando
        Optional<Property> foundProperty = propertyRepository.findById(999L); // ID que no existe

        // Entonces
        assertThat(foundProperty).isNotPresent();
    }

    @Test
    @DisplayName("Debe guardar y recuperar una propiedad")
    void save_shouldSaveAndRetrieveProperty() {
        // Dado
        Property newProperty = Property.builder()
                .title("Loft Urbano")
                .description("Amplio loft con diseño moderno.")
                .location("Palermo")
                .pricePerNight(95.0)
                .category("Loft")
                .latitude(new BigDecimal("-34.5828"))
                .longitude(new BigDecimal("-58.4239"))
                .numberOfRooms(1)
                .numberOfBathrooms(1)
                .size(new BigDecimal("60.0"))
                .features(new HashSet<>(Arrays.asList("Terraza")))
                .amenities(new HashSet<>(Arrays.asList("Gym")))
                .imageUrl("http://example.com/loft1.jpg")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();

        // Cuando
        Property savedProperty = propertyRepository.save(newProperty);
        Optional<Property> foundProperty = propertyRepository.findById(savedProperty.getId());

        // Entonces
        assertThat(foundProperty).isPresent();
        assertThat(foundProperty.get().getTitle()).isEqualTo("Loft Urbano");
    }

    @Test
    @DisplayName("Debe eliminar una propiedad por ID")
    void deleteById_shouldDeleteProperty() {
        // Cuando
        propertyRepository.deleteById(property1.getId());
        Optional<Property> foundProperty = propertyRepository.findById(property1.getId());

        // Entonces
        assertThat(foundProperty).isNotPresent();
        assertThat(propertyRepository.findAll()).hasSize(2);
    }
}