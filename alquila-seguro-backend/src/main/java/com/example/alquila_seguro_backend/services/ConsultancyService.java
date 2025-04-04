package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final EmailService emailService;
    private final Logger LOGGER =  LoggerFactory.getLogger(ConsultancyService.class.getName());
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
                .build();
    }
    @Transactional
    public ApiResponse<ConsultancyResponse> createConsultancy(ConsultancyCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId()).orElse(null);
        if (client == null) {
            return ApiResponse.<ConsultancyResponse>builder()
                    .success(false)
                    .message("Cliente no encontrado.")
                    .build();
        }

        Property property = propertyRepository.findById(request.getPropertyId()).orElse(null);
        if (property == null) {
            return ApiResponse.<ConsultancyResponse>builder()
                    .success(false)
                    .message("Propiedad no encontrada.")
                    .build();
        }

        Consultancy consultancy = Consultancy.builder()
                .client(client)
                .property(property)
                .details(request.getDetails())
                .requestedAt(LocalDateTime.now())
                .status(ConsultancyStatus.PENDING)
                .build();

        String subject = "Nueva Consultoría Creada";
        String body = "Se ha creado una nueva consultoría con los siguientes detalles:\n\n" +
                "Cliente: " + consultancy.getClient().getFirstName() + " " + consultancy.getClient().getLastName() + "\n" +
                "Correo electrónico: " + consultancy.getClient().getEmail() + "\n" +
                "Propiedad: " + consultancy.getProperty().getTitle() + "\n" +
                "Detalles: " + consultancy.getDetails();
        try{
            emailService.sendEmail("example@gmail.com", subject, body);

        } catch (Exception e) {
            LOGGER.error("Error al enviar el mail a la consultoria con id: {} ", consultancy.getId(), e.getMessage());
        }

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
