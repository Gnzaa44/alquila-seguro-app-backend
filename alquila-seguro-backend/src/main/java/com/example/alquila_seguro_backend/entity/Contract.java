package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un contrato de caución en el sistema de alquileres temporarios.
 * Almacena información importante sobre un contrato.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Contract {
    /**
     * Identificador único del contrato.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Contrato relacionado con una reserva.
     * Uno --> Uno
     */
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    /**
     * Lleva registro de cuando fue creado.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
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
