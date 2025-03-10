package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Invoice invoice;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Contract contract;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

    @NotNull(message = "start date cannot be null")
    private LocalDateTime startDate;

 
    @NotNull(message = "end date cannot be null")
    private LocalDateTime endDate;

    @NotNull(message = "status is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
