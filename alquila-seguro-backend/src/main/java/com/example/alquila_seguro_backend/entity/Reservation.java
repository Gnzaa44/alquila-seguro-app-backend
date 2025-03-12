package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(mappedBy = "reservation")
    private Invoice invoice;

    @OneToOne(mappedBy = "reservation")
    private Contract contract;

    @OneToOne(mappedBy = "reservation")
    private Payment payment;


    @FutureOrPresent(message = "The start date must be today or in the future")
    @NotNull(message = "start date cannot be null")
    private LocalDateTime startDate;

    @Future(message = "The end date must be in the future")
    @NotNull(message = "end date cannot be null")
    private LocalDateTime endDate;

    @NotNull(message = "status is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
