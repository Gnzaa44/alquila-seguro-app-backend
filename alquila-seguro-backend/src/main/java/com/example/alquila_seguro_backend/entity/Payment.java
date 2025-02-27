package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "consultancy_id")
    private Consultancy consultancy;

    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentDate;
    private String paymentReference;
    private String paymentDescription;

}
