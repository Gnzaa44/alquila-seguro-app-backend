package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultancyService {
    private final ConsultancyRepository consultancyRepository;
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private ClientResponse mapToClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .createdAt(client.getCreatedAt())
                .build();
    }
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
    private ConsultancyResponse mapToConsultancyResponse(Consultancy consultancy) {
        return ConsultancyResponse.builder()
                .id(consultancy.getId())
                .client(mapToClientResponse(consultancy.getClient()))
                .property(mapToPropertyResponse(consultancy.getProperty()))
                .details(consultancy.getDetails())
                .requestedAt(consultancy.getRequestedAt())
                .status(consultancy.getStatus())
                .build();
    }
    @Transactional
    public ApiResponse<ConsultancyResponse> createConsultancy(Long clientId, Long propertyId, String details) {
        Client client = clientRepository.findById(clientId).orElse(null);
        if (client == null) {
            return ApiResponse.<ConsultancyResponse>builder()
                    .success(false)
                    .message("Cliente no encontrado.")
                    .build();
        }

        Property property = propertyRepository.findById(propertyId).orElse(null);
        if (property == null) {
            return ApiResponse.<ConsultancyResponse>builder()
                    .success(false)
                    .message("Propiedad no encontrada.")
                    .build();
        }

        Consultancy consultancy = Consultancy.builder()
                .client(client)
                .property(property)
                .details(details)
                .requestedAt(LocalDateTime.now())
                .status(ConsultancyStatus.PENDING)
                .build();

        Consultancy savedConsultancy = consultancyRepository.save(consultancy);
        return ApiResponse.<ConsultancyResponse>builder()
                .success(true)
                .message("Consultoria creada correctamente.")
                .data(mapToConsultancyResponse(savedConsultancy))
                .build();
    }

    @Transactional
    public ApiResponse<ConsultancyResponse> updateConsultancyStatus(Long consultancyId, ConsultancyStatus status) {
        Consultancy consultancy = consultancyRepository.findById(consultancyId).orElse(null);
        if (consultancy == null) {
            return ApiResponse.<ConsultancyResponse>builder().success(false).message("Consultancy not found").build();
        }

        consultancy.setStatus(status);
        Consultancy updatedConsultancy = consultancyRepository.save(consultancy);
        return ApiResponse.<ConsultancyResponse>builder()
                .success(true)
                .message("Estado de la consultoria actualizado correctamente.")
                .data(mapToConsultancyResponse(updatedConsultancy))
                .build();
    }

    public ApiResponse<List<ConsultancyResponse>> getConsultanciesByClient(Long clientId) {
        List<ConsultancyResponse> consultancies = consultancyRepository.findByClientId(clientId).stream()
                .map(this::mapToConsultancyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ConsultancyResponse>>builder()
                .success(true)
                .message("Consultorias por cliente obtenidas correctamente.")
                .data(consultancies)
                .build();
    }

    public ApiResponse<List<ConsultancyResponse>> getConsultanciesByProperty(Long propertyId) {
        List<ConsultancyResponse> consultancies = consultancyRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToConsultancyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ConsultancyResponse>>builder()
                .success(true)
                .message("Consultorias por propiedad obtenidas correctamente.")
                .data(consultancies)
                .build();
    }

    public ApiResponse<List<ConsultancyResponse>> getConsultanciesByStatus(ConsultancyStatus status) {
        List<ConsultancyResponse> consultancies = consultancyRepository.findByStatus(status).stream()
                .map(this::mapToConsultancyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ConsultancyResponse>>builder()
                .success(true)
                .message("Consultorias por estado obtenidas correctamente.")
                .data(consultancies)
                .build();
    }

    public ApiResponse<ConsultancyResponse> getConsultancyById(Long consultancyId) {
        return consultancyRepository.findById(consultancyId)
                .map(consultancy -> ApiResponse.<ConsultancyResponse>builder()
                        .success(true)
                        .message("Consultoria obtenida correctamente.")
                        .data(mapToConsultancyResponse(consultancy))
                        .build())
                .orElse(ApiResponse.<ConsultancyResponse>builder()
                        .success(false)
                        .message("Consultoria no encontrada.")
                        .build());
    }

}
