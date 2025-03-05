package com.example.alquila_seguro_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import javax.lang.model.element.Name;
import java.time.LocalDateTime;

@Entity
@Table(name = "Consultancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Consultancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "details are required")
    @Size(min = 10, max = 500, message = "details must be between 10 and 500 characters")
    @Column(nullable = false)
    private String details;

    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @NotNull(message = "status is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsultancyStatus status;

}
enum ConsultancyStatus {
    PENDING, RESPONDED, CLOSED
}

