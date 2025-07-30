package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;

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
                .imageUrls(property.getImageUrls())
                .propertyStatus(property.getPropertyStatus())
                .build();
    }
    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        boolean hasInvoice = reservation.getInvoice() != null;
        boolean hasContract = reservation.getContract() != null;
        BigDecimal totalPrice = calculateTotalAmount(reservation.getProperty().getPricePerNight(), reservation.getStartDate(), reservation.getEndDate());
        return ReservationResponse.builder()
                .id(reservation.getId())
                .property(mapToPropertyResponse(reservation.getProperty()))
                .client(mapToClientResponse(reservation.getClient()))
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .status(reservation.getStatus())
                .hasInvoice(hasInvoice)
                .hasContract(hasContract)
                .totalAmount(totalPrice)
                .build();

    }

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


    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations = reservationRepository.findAll().stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ReservationResponse>>builder()
                .success(true)
                .message("Reservas recuperadas correctamente.")
                .data(reservations)
                .build();
    }

    public ApiResponse<List<ReservationResponse>> getReservationsByClientId(Long clientId) {
        List<ReservationResponse> reservations = reservationRepository.findByClientId(clientId).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ReservationResponse>>builder()
                .success(true)
                .message("Reservas por cliente recuperadas correctamente.")
                .data(reservations)
                .build();
    }

    public ApiResponse<List<ReservationResponse>> getReservationsByPropertyId(Long propertyId) {
        List<ReservationResponse> reservations = reservationRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ReservationResponse>>builder()
                .success(true)
                .message("Reservas por propiedad recuperadas correctamente.")
                .data(reservations)
                .build();
    }

    public ApiResponse<List<ReservationResponse>> getReservationsByStatus(ReservationStatus status) {
        List<ReservationResponse> reservations = reservationRepository.findByStatus(status).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ReservationResponse>>builder()
                .success(true)
                .message("Reservas por estado recuperadas correctamente.")
                .data(reservations)
                .build();
    }

    public ApiResponse<ReservationResponse> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> ApiResponse.<ReservationResponse>builder()
                        .success(true)
                        .message("Reserva obtenida correctamente.")
                        .data(mapToReservationResponse(reservation))
                        .build())
                .orElse(ApiResponse.<ReservationResponse>builder()
                        .success(false)
                        .message("Reserva con el id: " + id + " no encontrada.")
                        .build());
    }

    @Transactional
    public ApiResponse<ReservationResponse> createReservation(ReservationCreateRequest request) {

        // Check if property exists and is available
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElse(null);
        if (property == null) {
            return ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("Propiedad con el id: " + request.getPropertyId() + " no encontrada.")
                    .build();
        }

        if (property.getPropertyStatus() != PropertyStatus.AVAILABLE) {
            return ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("Propiedad no disponible para la reserva.")
                    .build();
        }
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("La fecha de inicio no puede ser posterior a la fecha de salida.")
                    .build();
        }

        // Check for overlapping reservations
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                property.getId(), request.getStartDate(), request.getEndDate());

        if (!overlappingReservations.isEmpty()) {
            return ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("La propiedad ya esta reservada para las fechas seleccionadas.")
                    .build();
        }
         Client client1 = clientRepository.findByEmail(request.getClientEmail()).orElseGet(() -> {
            Client newClient = Client.builder()
                    .firstName(request.getClientFirstName())
                    .lastName(request.getClientLastName())
                    .email(request.getClientEmail())
                    .phone(request.getClientPhone())
                    .build();
            return clientRepository.save(newClient);
        });

        Reservation reservation = Reservation.builder()
                .client(client1)
                .property(property)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);



        Invoice invoice = Invoice.builder()
                .reservation(savedReservation)
                .totalAmount(calculateTotalAmount(
                        savedReservation.getProperty().getPricePerNight(),
                        request.getStartDate(),
                        request.getEndDate()))
                .filePath(generateInvoiceFilePath(savedReservation.getId()))
                .issuedAt(LocalDateTime.now())
                .status(DocumentStatus.PENDING)
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);
        savedReservation.setInvoice(savedInvoice);

        Contract contract = Contract.builder()
                    .reservation(savedReservation)
                    .filePath(generateContractFilePath(savedReservation.getId()))// Generar ruta del archivo (simulado)
                    .status(DocumentStatus.PENDING)
                    .build();
            Contract savedContract = contractRepository.save(contract);
            savedReservation.setContract(savedContract);

        reservationRepository.save(savedReservation); // Guardar la reserva nuevamente con la factura y el contrato asociados

        return ApiResponse.<ReservationResponse>builder()
                .success(true)
                .message("Reserva creada correctamente.")
                .data(mapToReservationResponse(savedReservation))
                .build();
    }
    @Transactional
    public void updateReservationStatusByPayment(Long reservationId, String paymentStatusMP) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        reservationOptional.ifPresent(reservation -> {
            ReservationStatus newStatus = null;
            switch (paymentStatusMP.toLowerCase()) {
                case "approved":
                    newStatus = ReservationStatus.CONFIRMED;
                    break;
                case "pending":
                    newStatus = ReservationStatus.PENDING;
                    break;
                case "rejected", "cancelled", "refunded":
                    newStatus = ReservationStatus.CANCELLED;
                    break;
                default:
                    logger.warn("Estado de pago desconocido: {}", paymentStatusMP);
                    break;
            }
            if (newStatus != null && reservation.getStatus() != newStatus) {
                logger.info("Actualizando el estado de la reserva {} a {} debido al estado del pago: {}", reservationId, newStatus, paymentStatusMP);
                reservation.setStatus(newStatus);
                reservationRepository.save(reservation);
            }
        });
        if (reservationOptional.isEmpty()) {
            logger.warn("No se encontró la reserva con ID: {} para actualizar el estado por el pago.", reservationId);
        }
    }
    private BigDecimal calculateTotalAmount(double pricePerNight, LocalDateTime startDate, LocalDateTime endDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        return BigDecimal.valueOf(pricePerNight).multiply(BigDecimal.valueOf(days));
    }
    private String generateInvoiceFilePath(Long reservationId) {
        return "temp/terminos_condiciones_alquilaseguro.pdf";
    }

    private String generateContractFilePath(Long reservationId) {
        return "temp/terminos_condiciones_alquilaseguro.pdf";
    }

    @Transactional
    public ApiResponse<ReservationResponse> confirmReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.PENDING) {
                        return ApiResponse.<ReservationResponse>builder()
                                .success(false)
                                .message("Solo las reservas pendientes pueden ser confirmadas.")
                                .build();
                    }

                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    Reservation updatedReservation = reservationRepository.save(reservation);

                    return ApiResponse.<ReservationResponse>builder()
                            .success(true)
                            .message("Reserva confirmada.")
                            .data(mapToReservationResponse(updatedReservation))
                            .build();
                })
                .orElse(ApiResponse.<ReservationResponse>builder()
                        .success(false)
                        .message("Reserva con el id: " + id + " no encontrada.")
                        .build());
    }

    @Transactional
    public ApiResponse<ReservationResponse> cancelReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() == ReservationStatus.COMPLETED ||
                            reservation.getStatus() == ReservationStatus.CANCELLED) {
                        return ApiResponse.<ReservationResponse>builder()
                                .success(false)
                                .message("No se puede cancelar una reserva ya completada o cancelada.")
                                .build();
                    }

                    reservation.setStatus(ReservationStatus.CANCELLED);
                    Reservation updatedReservation = reservationRepository.save(reservation);
                    return ApiResponse.<ReservationResponse>builder()
                            .success(true)
                            .message("Reserva cancelada correctamente.")
                            .data(mapToReservationResponse(updatedReservation))
                            .build();
                })
                .orElse(ApiResponse.<ReservationResponse>builder()
                        .success(false)
                        .message("Reserva con el id: " + id + " no encontrada.")
                        .build());
    }

    @Transactional
    public ApiResponse<ReservationResponse> completeReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                        return ApiResponse.<ReservationResponse>builder()
                                .success(false)
                                .message("Solo las reservas confirmadas pueden ser completadas.")
                                .build();
                    }

                    reservation.setStatus(ReservationStatus.COMPLETED);
                    Reservation updatedReservation = reservationRepository.save(reservation);
                    return ApiResponse.<ReservationResponse>builder()
                            .success(true)
                            .message("Reserva completada.")
                            .data(mapToReservationResponse(updatedReservation))
                            .build();
                })
                .orElse(ApiResponse.<ReservationResponse>builder()
                        .success(false)
                        .message("Reserva con el id: " + id + " no encontrada.")
                        .build());
    }

}
