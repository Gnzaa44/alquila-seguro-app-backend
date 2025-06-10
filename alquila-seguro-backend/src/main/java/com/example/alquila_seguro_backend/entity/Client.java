package com.example.alquila_seguro_backend.entity;

import com.example.alquila_seguro_backend.validation.ArgentinianPhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
@ToString(exclude = {"reservations", "consultancies"}) // Suponiendo que las tienes
@Builder
/**
 * Entidad que representa a un cliente en el sistema de alquileres temporarios.
 * Almacena información para almacenar informacion sobre los clientes para ser manipulada.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
public class Client {
    /**
     * Identificador único del cliente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Lista de reservas que estaran relacionadas con el cliente.
     * Uno --> Muchas.
     */
    @OneToMany(mappedBy = "client")
    private List<Reservation> reservations;
    /**
     * Lista de consultorias que estaran relacionadas con el cliente.
     * Uno --> Muchas.
     */
    @OneToMany(mappedBy = "client")
    private List<Consultancy> consultancies;
    /**
     * Nombre del cliente.
     */
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    @Column(nullable = false)
    private String firstName;
    /**
     * Apellido del cliente.
     */
    @NotBlank(message = "El apellido es obligatorio.")
    @Size(min = 3, max = 100, message = "El apellido debe tener entre 3 y 100 caracteres.")
    @Column(nullable = false)
    private String lastName;
    /**
     * Email del cliente.
     */
    @Email(message = "El mail debe ser válido.")
    @NotBlank(message = "El mail es obligatorio.")
    @Column(nullable = false, unique = true)
    private String email;
    /**
     * Número de teléfono del cliente.
     */
    @ArgentinianPhoneNumber(message = "El número de teléfono debe ser un número válido de Argentina.")
    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(min = 10, max = 15, message = "El teléfono debe contener entre 10 y 15 caracteres.")
    @Column(nullable = false)
    private String phone;
    /**
     * Lleva registro de cuando fue creado el cliente.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}

