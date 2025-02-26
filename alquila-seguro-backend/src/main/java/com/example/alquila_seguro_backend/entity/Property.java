package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.awt.print.Book;
import java.util.List;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double price;
    private String imageUrl;
    private boolean available;

    @OneToMany(mappedBy = "property")
    private List<Booking>bookings;

}
