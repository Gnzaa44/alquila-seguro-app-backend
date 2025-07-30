package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa a un pago dentro del sistema de alquileres temporarios.
 * Almacena información adicional sobre los pagos realizados.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name="payments", uniqueConstraints = {
@UniqueConstraint(name = "uk_payments_external_id_type_status ", columnNames = {"external_id", "external_entity_type", "payment_status"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Payment {
    /**
     * Identificador único del pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Pagos relacionados a una reserva.
     * Muchos --> Uno.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true) // 'nullable = true' si es opcional
    private Reservation reservation;
    /**
     * Pagos relacionados a una consultoria.
     * Muchos --> Uno.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultancy_id", nullable = true) // 'nullable = true' si es opcional
    private Consultancy consultancy;
    /**
     * Monto del pago.
     */
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0.")
    @Column(nullable = false)
    private BigDecimal amount;
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
    /**
     * Id del pago generado por Mercado Pago.
     */
    @Column(nullable = true)
    private String paymentIdMP;

    private String externalId; // ID de la reserva o consultoría

    @Enumerated(EnumType.STRING) // Guarda el nombre del enum como String en la DB
    private ExternalEntityType externalEntityType;
}
