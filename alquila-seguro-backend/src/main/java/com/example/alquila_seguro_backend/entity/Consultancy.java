package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa a una consultoria en el sistema de alquileres temporarios.
 * Almacena información sobre las consultorias.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 21/3/2025
 */
@Entity
@Table(name = "consultancies")
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
     * Consultoria asociada a muchos pagos.
     * Uno --> Muchos.
     */
    @OneToMany(mappedBy = "consultancy", fetch = FetchType.LAZY) // Sin cascade ni orphanRemoval
    private Set<Payment> payments = new HashSet<>();
    /**
     * Motivos de la consultoria realizada.
     */
    @NotBlank(message = "Los detalles son obligatorios.")
    @Size(min = 10, max = 500, message = "Detalles debe contener entre 10 y 500 caracteres.")
    @Column(nullable = false)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
    /**
     * Estados posibles de la consultoria.
     */
    @NotNull(message = "El estado es obligatorio.")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsultancyStatus status;
}

