package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa a una factura en el sistema de alquileres temporarios.
 * Almacena información importante sobre una factura.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Invoice {
    /**
     * Identificador único de la factura.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Factura asociada a una reserva.
     * Uno --> Uno
     */
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    /**
     * Monto total incluido.
     */
    @NotNull(message = "El monto total es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor que 0.")
    @Column(nullable = false)
    private BigDecimal totalAmount;
    /**
     * Lleva registro de cuando fue emitida.
     */
    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();
    /**
     * Almacenará el vínculo del pdf correspondiente.
     */
    @Column(nullable = false)
    private String filePath;
    /**
     * Estados posibles del documento.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

}
