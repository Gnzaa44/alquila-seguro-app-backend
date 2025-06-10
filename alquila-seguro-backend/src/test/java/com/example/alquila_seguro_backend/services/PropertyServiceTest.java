package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.PropertyCreateRequest;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyService propertyService;

    private Property property1;
    private Property property2;
    private PropertyCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        property1 = Property.builder()
                .id(1L)
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
                .id(2L)
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

        createRequest = PropertyCreateRequest.builder()
                .title("Nueva Propiedad")
                .description("Descripción de la nueva propiedad.")
                .location("Rosario")
                .pricePerNight(100.0)
                .category("Casa")
                .latitude(new BigDecimal("-32.9468"))
                .longitude(new BigDecimal("-60.6393"))
                .numberOfRooms(2)
                .numberOfBathrooms(1)
                .size(new BigDecimal("80.0"))
                .features(new HashSet<>(Collections.singletonList("Patio")))
                .amenities(new HashSet<>(Collections.singletonList("Parrilla")))
                .imageUrl("http://example.com/new.jpg")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("Debe crear una propiedad exitosamente")
    void createProperty_shouldReturnSuccessResponse() {
        // Dado
        when(propertyRepository.save(any(Property.class))).thenReturn(property1);

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.createProperty(createRequest);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("La propiedad se ha creado correctamente.");
        assertThat(response.getData().getTitle()).isEqualTo(property1.getTitle());
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("Debe obtener todas las propiedades exitosamente")
    void getAllProperties_shouldReturnAllProperties() {
        // Dado
        List<Property> properties = Arrays.asList(property1, property2);
        when(propertyRepository.findAll()).thenReturn(properties);

        // Cuando
        ApiResponse<List<PropertyResponse>> response = propertyService.getAllProperties();

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedades obtenidas correctamente.");
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getTitle()).isEqualTo(property1.getTitle());
        verify(propertyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener solo propiedades disponibles")
    void getAvailableProperties_shouldReturnOnlyAvailableProperties() {
        // Dado
        List<Property> availableProperties = Collections.singletonList(property1);
        when(propertyRepository.findByPropertyStatus(PropertyStatus.AVAILABLE)).thenReturn(availableProperties);

        // Cuando
        ApiResponse<List<PropertyResponse>> response = propertyService.getAvailableProperties();

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedades disponibles obtenidas correctamente.");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getPropertyStatus()).isEqualTo(PropertyStatus.AVAILABLE);
        verify(propertyRepository, times(1)).findByPropertyStatus(PropertyStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe obtener propiedades por categoría")
    void getPropertiesByCategory_shouldReturnPropertiesOfGivenCategory() {
        // Dado
        List<Property> categoryProperties = Collections.singletonList(property1);
        when(propertyRepository.findByCategory(anyString())).thenReturn(categoryProperties);

        // Cuando
        ApiResponse<List<PropertyResponse>> response = propertyService.getPropertiesByCategory("Casa");

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedades por categoria obtenidas correctamente.");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCategory()).isEqualTo("Casa");
        verify(propertyRepository, times(1)).findByCategory(anyString());
    }

    @Test
    @DisplayName("Debe obtener propiedades por precio máximo")
    void getPropertiesByMaxPrice_shouldReturnPropertiesBelowMaxPrice() {
        // Dado
        List<Property> priceProperties = Collections.singletonList(property2);
        when(propertyRepository.findByPricePerNightLessThanEqual(anyDouble())).thenReturn(priceProperties);

        // Cuando
        ApiResponse<List<PropertyResponse>> response = propertyService.getPropertiesByMaxPrice(100.0);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedades por precio maximo obtenidas correctamente.");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getPricePerNight()).isLessThanOrEqualTo(100.0);
        verify(propertyRepository, times(1)).findByPricePerNightLessThanEqual(anyDouble());
    }

    @Test
    @DisplayName("Debe obtener propiedades por ubicación")
    void getPropertiesByLocation_shouldReturnPropertiesInGivenLocation() {
        // Dado
        List<Property> locationProperties = Collections.singletonList(property2);
        when(propertyRepository.findByLocationContaining(anyString())).thenReturn(locationProperties);

        // Cuando
        ApiResponse<List<PropertyResponse>> response = propertyService.getPropertiesByLocation("Buenos Aires");

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedades por localizacion obtenidas correctamente.");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getLocation()).contains("Buenos Aires");
        verify(propertyRepository, times(1)).findByLocationContaining(anyString());
    }

    @Test
    @DisplayName("Debe obtener una propiedad por ID existente")
    void getPropertyById_shouldReturnPropertyWhenExists() {
        // Dado
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property1));

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.getPropertyById(1L);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedad obtenida correctamente.");
        assertThat(response.getData().getId()).isEqualTo(1L);
        verify(propertyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar un error si la propiedad no existe por ID")
    void getPropertyById_shouldReturnErrorWhenNotExists() {
        // Dado
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.getPropertyById(999L);

        // Entonces
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("no encontrada");
        assertThat(response.getData()).isNull();
        verify(propertyRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe actualizar una propiedad existente exitosamente")
    void updateProperty_shouldReturnSuccessResponseWhenPropertyExists() {
        // Dado
        PropertyCreateRequest updateRequest = PropertyCreateRequest.builder()
                .title("Casa de Playa Actualizada")
                .description("Descripción actualizada.")
                .location("Mar del Plata")
                .pricePerNight(160.0)
                .category("Casa")
                .latitude(new BigDecimal("-38.0055"))
                .longitude(new BigDecimal("-57.5426"))
                .numberOfRooms(3)
                .numberOfBathrooms(2)
                .size(new BigDecimal("120.5"))
                .features(new HashSet<>(Arrays.asList("Pileta", "Jardín", "Actualizado")))
                .amenities(new HashSet<>(Arrays.asList("Wifi")))
                .imageUrl("http://example.com/casa1_updated.jpg")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property1));
        when(propertyRepository.save(any(Property.class))).thenReturn(property1); // Simula que save devuelve la misma propiedad

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.updateProperty(1L, updateRequest);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedad actualizada correctamente.");
        assertThat(response.getData().getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(response.getData().getPricePerNight()).isEqualTo(updateRequest.getPricePerNight());
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("Debe retornar error al actualizar si la propiedad no existe")
    void updateProperty_shouldReturnErrorResponseWhenPropertyDoesNotExist() {
        // Dado
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.updateProperty(999L, createRequest);

        // Entonces
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("no encontrada");
        assertThat(response.getData()).isNull();
        verify(propertyRepository, times(1)).findById(999L);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Debe actualizar el estado de una propiedad existente exitosamente")
    void updatePropertyByStatus_shouldReturnSuccessResponseWhenPropertyExists() {
        // Dado
        Property updatedStatusProperty = Property.builder()
                .id(1L).title("Casa en la Playa").propertyStatus(PropertyStatus.RESERVED) // Simula el estado actualizado
                .build();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property1));
        when(propertyRepository.save(any(Property.class))).thenReturn(updatedStatusProperty);

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.updatePropertyByStatus(1L, PropertyStatus.RESERVED);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Estado de la propiedad actualizado correctamente.");
        assertThat(response.getData().getPropertyStatus()).isEqualTo(PropertyStatus.RESERVED);
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("Debe retornar error al actualizar el estado si la propiedad no existe")
    void updatePropertyByStatus_shouldReturnErrorResponseWhenPropertyDoesNotExist() {
        // Dado
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Cuando
        ApiResponse<PropertyResponse> response = propertyService.updatePropertyByStatus(999L, PropertyStatus.AVAILABLE);

        // Entonces
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("no actualizada");
        assertThat(response.getData()).isNull();
        verify(propertyRepository, times(1)).findById(999L);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Debe eliminar una propiedad existente exitosamente")
    void deleteProperty_shouldReturnSuccessResponseWhenPropertyExists() {
        // Dado
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property1));
        doNothing().when(propertyRepository).delete(property1);

        // Cuando
        ApiResponse<Void> response = propertyService.deleteProperty(1L);

        // Entonces
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Propiedad eliminada correctamente.");
        assertThat(response.getData()).isNull();
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).delete(property1);
    }

    @Test
    @DisplayName("Debe retornar error al eliminar si la propiedad no existe")
    void deleteProperty_shouldReturnErrorResponseWhenPropertyDoesNotExist() {
        // Dado
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Cuando
        ApiResponse<Void> response = propertyService.deleteProperty(999L);

        // Entonces
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("no encontrada");
        assertThat(response.getData()).isNull();
        verify(propertyRepository, times(1)).findById(999L);
        verify(propertyRepository, never()).delete(any(Property.class));
    }
}