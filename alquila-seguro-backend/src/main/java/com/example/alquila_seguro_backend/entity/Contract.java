package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, length = 1000) // Asegura que el contrato no sea demasiado corto
    private String terms;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
