package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import javax.lang.model.element.Name;

@Entity
@Table(name = "Consultancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Consultancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String customerEmail;
    private String propertyLink;
    private String queryReason;
    private String dateRequested;
    private Boolean isPaid;

    public Consultancy(Long consultancyId) {
        this.id = consultancyId;
    }
}
