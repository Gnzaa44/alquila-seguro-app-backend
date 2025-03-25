package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un pago dentro del sistema de alquileres temporarios.
 * Almacena información adicional sobre los pagos realizados.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name="payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Payment {
    /**
     * Identificador único del pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Reserva relacionada a un pago.
     * Uno --> Uno (Opcional).
     */
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = true)
    private Reservation reservation;
    /**
     * Consultoria relacionada a un pago.
     * Uno --> Uno (opcional).
     */
    @OneToOne
    @JoinColumn(name = "consultancy_id", nullable = true)
    private Consultancy consultancy;
    /**
     * Monto total del pago.
     */
    @DecimalMin(value = "0.01", message = "total amount must be greater than 0")    @Column(nullable = false)
    private Double amount;
    /**
     * Metodo de pago elegido.
     */
    @Column(nullable = false)
    private String paymentMethod;
    /**
     * Fecha de pago registrada.
     */
    @Column(nullable = false)
    private LocalDateTime paymentDate;
    /**
     * Referencia de pago.
     */
    @Column(nullable = false)
    private String paymentReference;
    /**
     * Descripcion adicional del pago.
     */
    @Column(nullable = false)
    private String paymentDescription;
    /**
     * Estados posibles de la transaccion.
     */
    @NotNull(message = "El estado es obligatorio.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

}
