package com.example.alquila_seguro_backend.entity;

import com.example.alquila_seguro_backend.validation.ArgentinianPhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Entidad que representa a un cliente en el sistema de alquileres temporarios.
 * Almacena información sobre los clientes.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 11/3/2025
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Client {
    /**
     * Identificador único del cliente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "client")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "client")
    private List<Consultancy> consultancies;

    @NotBlank(message = "firstname is required")
    @Size(min = 3, max = 100, message = "fistname must be between 3 and 100 characters long")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "lastname is required")
    @Size(min = 3, max = 100, message = "lastname must be between 3 and 100 characters long")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "mail must be valid")
    @NotBlank(message = "mail is required")
    @Column(nullable = false, unique = true)
    private String email;

    @ArgentinianPhoneNumber(message = "El número de teléfono debe ser un número válido de Argentina.")
    @NotBlank(message = "phone is required")
    @Size(min = 10, max = 15, message = "phone must be between 10 and 15 characters long")
    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}

