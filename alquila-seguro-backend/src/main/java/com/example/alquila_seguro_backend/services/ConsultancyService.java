package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import com.example.alquila_seguro_backend.repositories.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailService emailService;
    @Value("${VEEDOR_EMAIL_DEV}")
    private String emailVeedor;
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
    private ConsultancyResponse mapToConsultancyResponse(Consultancy consultancy) {
        return ConsultancyResponse.builder()
                .id(consultancy.getId())
                .client(mapToClientResponse(consultancy.getClient()))
                .details(consultancy.getDetails())
                .requestedAt(consultancy.getRequestedAt())
                .build();
    }
    @Transactional
    public ApiResponse<ConsultancyResponse> createConsultancy(ConsultancyCreateRequest request) {
        Client client = clientRepository.findByEmail(request.getClientEmail()).orElseGet(() -> {
            Client newClient = Client.builder()
                    .firstName(request.getClientFirstName())
                    .lastName(request.getClientLastName())
                    .email(request.getClientEmail())
                    .phone(request.getClientPhone())
                    .build();
            return clientRepository.save(newClient);
        });

        Consultancy consultancy = Consultancy.builder()
                .client(client)
                .details(request.getDetails())
                .requestedAt(LocalDateTime.now())
                .status(ConsultancyStatus.PENDING)
                .build();

        Consultancy savedConsultancy = consultancyRepository.save(consultancy);

        String subject = "Nueva Consultoría Solicitada";
        String body = "Se ha solicitado una nueva consultoría con los siguientes detalles:\n\n" +
                "Cliente: " + savedConsultancy.getClient().getFirstName() + " " + savedConsultancy.getClient().getLastName() + "\n" +
                "Correo electrónico: " + savedConsultancy.getClient().getEmail() + "\n" +
                "Detalles: " + savedConsultancy.getDetails() + "\n" +
                "Un veedor se contactara lo antes posible.";
        try{
            emailService.sendEmail(emailVeedor, subject, body);

        } catch (Exception e) {
            LOGGER.warn("Error al enviar el mail a la consultoria con id: {} ", savedConsultancy.getId(), e);
        }
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
    public ApiResponse<List<ConsultancyResponse>> getAllConsultancies() {
        List<ConsultancyResponse> consultancies = consultancyRepository.findAll().stream()
                .map(this::mapToConsultancyResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ConsultancyResponse>>builder()
                .success(true)
                .message("Consultorias recuperadas correctamente.")
                .data(consultancies)
                .build();
    }


}
