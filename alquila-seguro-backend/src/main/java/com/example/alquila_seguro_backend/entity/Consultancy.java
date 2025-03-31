package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import javax.lang.model.element.Name;
import java.time.LocalDateTime;

/**
 * Entidad que representa a una consultoria en el sistema de alquileres temporarios.
 * Almacena información sobre las consultorias.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 21/3/2025
 */
@Entity
@Table(name = "Consultancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Consultancy {
    /**
     * Identificador único de la consultoria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Consultorias relacionadas con un cliente.
     * Muchas --> Uno
     */
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    /**
     * Consultorias relacionadas con una propiedad.
     * Muchas --> Una
     */
    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;
    /**
     * Consultoria asociada a un pago.
     * Uno --> Uno
     */
    @OneToOne(mappedBy = "consultancy")
    private Payment payment;
    /**
     * Motivos de la consultoria realizada.
     */
    @NotBlank(message = "Los detalles son obligatorios.")
    @Size(min = 10, max = 500, message = "Detalles debe contener entre 10 y 500 caracteres.")
    @Column(nullable = false)
    private String details;
    /**
     * Consultorias relacionadas con una propiedad.
     * Muchas --> Una
     */
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
    /**
     * Estados posibles de la consultoria.
     */
    @NotNull(message = "El estado es obligatorio.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsultancyStatus status;
}

