package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = true)
    private Reservation reservation;

    @OneToOne
    @JoinColumn(name = "consultancy_id", nullable = true)
    private Consultancy consultancy;

    @DecimalMin(value = "0.01", message = "total amount must be greater than 0")    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String paymentStatus;
    @Column(nullable = false)
    private LocalDateTime paymentDate;
    @Column(nullable = false)
    private String paymentReference;
    @Column(nullable = false)
    private String paymentDescription;

}
