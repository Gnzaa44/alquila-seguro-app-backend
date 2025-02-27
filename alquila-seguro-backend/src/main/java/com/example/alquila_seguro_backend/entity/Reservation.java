package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String startDate;
    private String endDate;
    private Double totalAmount;

    private Boolean confirmed;
    private String paymentStatus;

    public Reservation(Long bookingId) {
        this.id = bookingId;
    }
}
