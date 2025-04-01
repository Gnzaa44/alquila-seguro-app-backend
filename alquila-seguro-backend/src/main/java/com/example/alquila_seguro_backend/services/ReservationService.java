package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;


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
    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        boolean hasInvoice = reservation.getInvoice() != null;
        boolean hasContract = reservation.getContract() != null;
        return ReservationResponse.builder()
                .id(reservation.getId())
                .property(mapToPropertyResponse(reservation.getProperty()))
                .client(mapToClientResponse(reservation.getClient()))
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .hasInvoice(hasInvoice)
                .hasContract(hasContract)
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
        // Check if client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElse(null);
        if (client == null) {
            return ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("Cliente con el id: " + request.getClientId() + " no encontrado.")
                    .build();
        }

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

        Reservation reservation = Reservation.builder()
                .client(client)
                .property(property)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return ApiResponse.<ReservationResponse>builder()
                .success(true)
                .message("Reserva creada correctamente.")
                .data(mapToReservationResponse(savedReservation))
                .build();
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

                    // Create invoice and contract for the confirmed reservation
                  //  generateInvoiceAndContract(updatedReservation);

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
