package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
@ToString(exclude = {"property"}) // <-- ¡Añade esto!
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
     * Reserva asociada a muchos pagos.
     * Uno --> Uno
     */
    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY) // Sin cascade ni orphanRemoval
    private Set<Payment> payments = new HashSet<>();
    /**
     * Fecha de inicio de la reserva.
     */
    @FutureOrPresent(message = "La fecha de entrada debe ser hoy o en el futuro.")
    @NotNull(message = "La fecha de entrada no puede ser vacia.")
    private LocalDateTime startDate;
    /**
     * Fecha de fin de la reserva.
     */
    @Future(message = "La fecha de salida debe ser en el futuro.")
    @NotNull(message = "La fecha de salida no puede ser vacia.")
    private LocalDateTime endDate;
    /**
     * Posibles estados de la reserva.
     */
    @NotNull(message = "El estado es requerido.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
