package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.PropertyCreateRequest;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.entity.PropertyType;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public ApiResponse<PropertyResponse> createProperty(PropertyCreateRequest property) {
        Property property1 = Property.builder()
                .title(property.getTitle())
                .description(property.getDescription())
                .location(property.getLocation())
                .pricePerNight(property.getPricePerNight())
                .category(property.getCategory())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .numberOfRooms(property.getNumberOfRooms())
                .numberOfBathrooms(property.getNumberOfBathrooms())
                .size(property.getSize())
                .features(property.getFeatures())
                .amenities(property.getAmenities())
                .imageUrl(property.getImageUrl())
                .propertyStatus(property.getPropertyStatus())
                .propertyType(property.getPropertyType())
                .build();
        Property savedProperty = propertyRepository.save(property1);
        return ApiResponse.<PropertyResponse>builder()
                .success(true)
                .message("La propiedad se ha creado correctamente.")
                .data(mapToPropertyResponse(savedProperty))
                .build();

    }

    public ApiResponse<List<PropertyResponse>> getAllProperties() {
        List<PropertyResponse> properties = propertyRepository.findAll().stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades obtenidas correctamente.")
                .data(properties)
                .build();
    }

    public ApiResponse<List<PropertyResponse>> getAvailableProperties() {
        List<PropertyResponse> properties = propertyRepository.findByStatus(PropertyStatus.AVAILABLE).stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades disponibles obtenidas correctamente.")
                .data(properties)
                .build();
    }

    public ApiResponse<List<PropertyResponse>> getPropertiesByCategory(String category) {
        List<PropertyResponse> properties = propertyRepository.findByCategory(category).stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades por categoria obtenidas correctamente.")
                .data(properties)
                .build();
    }

    public ApiResponse<List<PropertyResponse>> getPropertiesByMaxPrice(Double maxPrice) {
        List<PropertyResponse> properties = propertyRepository.findByPricePerNightLessThanEqual(maxPrice).stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades por precio maximo obtenidas correctamente.")
                .data(properties)
                .build();
    }

    public ApiResponse<List<PropertyResponse>> getPropertiesByLocation(String location) {
        List<PropertyResponse> properties = propertyRepository.findByLocationContaining(location).stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades por localizacion obtenidas correctamente.")
                .data(properties)
                .build();
    }

    public ApiResponse<PropertyResponse> getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .map(property -> ApiResponse.<PropertyResponse>builder()
                        .success(true)
                        .message("Propiedad obtenida correctamente.")
                        .data(mapToPropertyResponse(property))
                        .build())
                .orElse(ApiResponse.<PropertyResponse>builder()
                        .success(false)
                        .message("Propiedad con el id: " + id + " no encontrada.")
                        .build());
    }
    @Transactional
    public ApiResponse<PropertyResponse> updateProperty(Long id, PropertyCreateRequest request) {
        return propertyRepository.findById(id)
                .map(property -> {
                    property.setTitle(request.getTitle());
                    property.setDescription(request.getDescription());
                    property.setLocation(request.getLocation());
                    property.setPricePerNight(request.getPricePerNight());
                    property.setCategory(request.getCategory());
                    property.setLatitude(request.getLatitude());
                    property.setLongitude(request.getLongitude());
                    property.setNumberOfRooms(request.getNumberOfRooms());
                    property.setNumberOfBathrooms(request.getNumberOfBathrooms());
                    property.setSize(request.getSize());
                    property.setFeatures(request.getFeatures());
                    property.setAmenities(request.getAmenities());
                    property.setImageUrl(request.getImageUrl());

                    if (request.getPropertyStatus() != null) {
                        property.setPropertyStatus(request.getPropertyStatus());
                    }
                    if (request.getPropertyType() != null) {
                        property.setPropertyType(request.getPropertyType());
                    }
                    Property updatedProperty = propertyRepository.save(property);
                    return ApiResponse.<PropertyResponse>builder()
                            .success(true)
                            .message("Propiedad actualizada correctamente.")
                            .data(mapToPropertyResponse(updatedProperty))
                            .build();
                })
                .orElse(ApiResponse.<PropertyResponse>builder()
                        .success(false)
                        .message("Propiedad con el id: " + id + " no encontrada.")
                        .build());
    }
    @Transactional
    public ApiResponse<PropertyResponse> updatePropertyByStatus(Long id, PropertyStatus status) {
        return propertyRepository.findById(id)
                .map(property -> {
                    property.setPropertyStatus(status);
                    Property updatedProperty = propertyRepository.save(property);
                    return ApiResponse.<PropertyResponse>builder()
                            .success(true)
                            .message("Estado de la propiedad actualizado correctamente.")
                            .data(mapToPropertyResponse(updatedProperty))
                            .build();
                })
                .orElse(ApiResponse.<PropertyResponse>builder()
                        .success(false)
                        .message("Propiedad con el id: " + id + " no actualizada.")
                        .build());

    }

    @Transactional
    public ApiResponse<Void> deleteProperty(Long id) {
        return propertyRepository.findById(id)
                .map(property -> {
                    propertyRepository.delete(property);
                    return ApiResponse.<Void>builder()
                            .success(true)
                            .message("Propiedad eliminada correctamente.")
                            .build();
                })
                .orElse(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Propiedad con el id: " + id + " no encontrada.")
                        .build());
    }
    public ApiResponse<List<PropertyResponse>> getByPropertyType(PropertyType propertyType) {
        List<Property> properties = propertyRepository.findByType(propertyType);
        List<PropertyResponse> propertyResponses = properties.stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades por tipo filtradas correctamente.")
                .data(propertyResponses)
                .build();

    }


    
}
