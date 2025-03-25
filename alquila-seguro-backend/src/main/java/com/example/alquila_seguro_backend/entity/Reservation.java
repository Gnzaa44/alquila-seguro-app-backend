package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa a la reserva dentro del sistema de alquileres temporarios.
 * Almacena información sobre las reservas.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Reservation {
    /**
     * Identificador único de la reserva.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Reservas asociadas a una propiedad.
     * Muchos --> Uno
     */
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    /**
     * Reservas asociadas a un cliente.
     * Muchos --> Uno
     */
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    /**
     * Reserva asociada a una factura.
     * Uno --> Uno
     */
    @OneToOne(mappedBy = "reservation")
    private Invoice invoice;
    /**
     * Reserva asociada a un contrato de caucion.
     * Uno --> Uno
     */
    @OneToOne(mappedBy = "reservation")
    private Contract contract;
    /**
     * Reserva asociada a un pago.
     * Uno --> Uno
     */
    @OneToOne(mappedBy = "reservation")
    private Payment payment;
    /**
     * Fecha de inicio de la reserva.
     */
    @FutureOrPresent(message = "The start date must be today or in the future")
    @NotNull(message = "start date cannot be null")
    private LocalDateTime startDate;
    /**
     * Fecha de fin de la reserva.
     */
    @Future(message = "The end date must be in the future")
    @NotNull(message = "end date cannot be null")
    private LocalDateTime endDate;
    /**
     * Posibles estados de la reserva.
     */
    @NotNull(message = "status is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
