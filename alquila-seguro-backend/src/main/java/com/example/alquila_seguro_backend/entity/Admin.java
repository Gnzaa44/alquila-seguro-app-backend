package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entidad que representa a un administrador en el sistema de alquileres temporarios.
 * Almacena información para gestionar el inicio de sesión.
 *
 * @author Gonzalo
 * @version 1.0
 * @since 12/3/2025
 */
@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Admin {
    /**
     * Identificador único del administrador.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    /**
     * Nombre de usuario del administrador.
     */
    @NotBlank(message = "Nombre de usuario obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres.")
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    /**
     * Contraseña del administrador
     */
    @NotBlank(message = "Contraseña obligatoria.")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres.")
    @Column(name = "password", nullable = false)
    private String password;
}
