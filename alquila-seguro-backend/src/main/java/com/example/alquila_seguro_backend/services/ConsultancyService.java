package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultancyService {
    private static final Logger logger = LoggerFactory.getLogger(ConsultancyService.class);
    private final ConsultancyRepository consultancyRepository;
    private final ClientRepository clientRepository;
    @Value("${VEEDOR_EMAIL}")
    private String veedorEmail;
    private final EmailService emailService;
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
                .status(consultancy.getStatus())
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

        return ApiResponse.<ConsultancyResponse>builder()
                .success(true)
                .message("Consultoria creada correctamente.")
                .data(mapToConsultancyResponse(savedConsultancy))
                .build();
    }

    @Transactional
    public ApiResponse<ConsultancyResponse> updateConsultancyStatus(Long consultancyId, ConsultancyStatus status) {
        Consultancy consultancy = consultancyRepository.findById(consultancyId)
                .orElseThrow(() -> new EntityNotFoundException("Consultoria no encontrada con ID: " + consultancyId)); // <-- CAMBIO CLAVE

        // Lógica de negocio para validar el cambio de estado
        if (consultancy.getStatus() == ConsultancyStatus.CLOSED && status == ConsultancyStatus.PENDING) {
            throw new IllegalArgumentException("El estado de la consultoría no puede ser modificado a PENDING si ya está en CLOSED."); // <-- CAMBIO CLAVE para el 400
        }
        consultancy.setStatus(status);
        Consultancy updatedConsultancy = consultancyRepository.save(consultancy);
        return ApiResponse.<ConsultancyResponse>builder()
                .success(true)
                .message("Estado de la consultoria actualizado correctamente.")
                .data(mapToConsultancyResponse(updatedConsultancy))
                .build();
    }
    @Transactional
    public void updateConsultancyStatusByPayment(Long consultancyId, String paymentStatusMP) {
        Optional<Consultancy> consultancyOptional = consultancyRepository.findById(consultancyId);

        consultancyOptional.ifPresent(consultancy -> {
            ConsultancyStatus oldStatus = consultancy.getStatus();
            ConsultancyStatus newStatus = null;
            switch (paymentStatusMP.toLowerCase()) {
                case "approved":
                    newStatus = ConsultancyStatus.CONFIRMED;
                    break;
                case "pending", "refunded", "cancelled", "rejected":
                    newStatus = ConsultancyStatus.PENDING;
                    break;
                default:
                    logger.warn("Estado de pago desconocido: {}", paymentStatusMP);
                    break;
            }
            if (newStatus != null) {
                // Bandera para decidir si se enviarán los emails
                boolean shouldSendEmails = false;

                // Si la consultoría pasa a CONFIRMED y el email no se ha enviado
                if (newStatus == ConsultancyStatus.CONFIRMED && oldStatus != ConsultancyStatus.CONFIRMED && !consultancy.isConfirmationEmailSent()) {
                    shouldSendEmails = true;
                }

                // Actualiza el estado de la consultoría si ha cambiado
                if (oldStatus != newStatus) {
                    logger.info("Actualizando el estado de la consultoría {} de {} a {} debido al estado del pago: {}", consultancyId, oldStatus, newStatus, paymentStatusMP);
                    consultancy.setStatus(newStatus);
                }

                // --- ¡CAMBIO CLAVE: MARCAR EL FLAG Y GUARDAR ANTES DE ENVIAR EMAILS! ---
                if (shouldSendEmails) {
                    consultancy.setConfirmationEmailSent(true); // Marca el flag aquí
                }
                // Guarda la consultoría (con el nuevo estado y/o el flag de email enviado)
                // Esto persiste el flag 'true' en la DB antes de que el email se dispare.
                consultancyRepository.save(consultancy);
                // -----------------------------------------------------------------------

                // Ahora, si se decidió que los emails deben enviarse, se disparan.
                if (shouldSendEmails) {
                    Client client = consultancy.getClient();
                    if (client != null && client.getEmail() != null) {
                        try {
                            emailService.sendConsultancyPaidToClient(consultancy);
                            if (veedorEmail != null && !veedorEmail.isEmpty()) {
                                emailService.sendConsultancyPaidToVeedor(consultancy, veedorEmail);
                            }
                            logger.info("Emails de confirmación de consultoría enviados para ID {} a {}.", consultancyId, client.getEmail());
                        } catch (Exception e) {
                            logger.error("Error al enviar emails de confirmación para consultoría ID {}: {}", consultancyId, e.getMessage(), e);
                            // Importante: Si el email falla en este punto, el flag ya está en true.
                            // Considera una estrategia de reintento externa o una notificación si esto es un problema crítico.
                            // Para este caso, solo loggear y no revertir el flag suele ser aceptable.
                        }
                    } else {
                        logger.warn("No se pudieron enviar emails para consultoría ID {}: Cliente o email no encontrado/válido.", consultancyId);
                    }
                }
            }
        });
        if (consultancyOptional.isEmpty()) {
            logger.warn("No se encontró la consultoria con ID: {} para actualizar el estado por el pago.", consultancyId);
        }
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
                .orElseThrow(() -> new EntityNotFoundException("Consultoría no encontrada con ID: " + consultancyId)); // <-- CAMBIO CLAVE

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
