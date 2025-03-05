package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) // Asegura que este campo no puede ser nulo
    private String username;
    @Column(nullable = false)
    private String password;
    @Email(message = "mail must be valid")
    @NotBlank(message = "mail is required")
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // Asegura que el rol no puede ser nulo
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
enum UserRole {
    CLIENT, ADMIN
}
