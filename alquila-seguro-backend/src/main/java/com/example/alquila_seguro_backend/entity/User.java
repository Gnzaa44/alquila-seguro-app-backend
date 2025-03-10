package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user")
    private List<Consultancy> consultancies;


    @NotBlank(message = "name is required")
    @Size(min = 3, max = 100, message = "name must be between 3 and 100 characters long")
    @Column(nullable = false)
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters long")
    @Column(nullable = false)
    private String password;
    @Email(message = "mail must be valid")
    @NotBlank(message = "mail is required")
    @Column(nullable = false, unique = true)
    private String email;
    @NotBlank(message = "phone is required")
    @Size(min = 10, max = 15, message = "phone must be between 10 and 15 characters long")
    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "role is required")
    @Column(nullable = false) 
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
enum UserRole {
    CLIENT, ADMIN
}
