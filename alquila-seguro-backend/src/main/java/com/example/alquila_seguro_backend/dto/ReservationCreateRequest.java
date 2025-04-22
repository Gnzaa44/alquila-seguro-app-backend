package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.ReservationStatus;
import com.example.alquila_seguro_backend.validation.ArgentinianPhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequest {
    @NotNull(message = "El ID de la propiedad es obligatorio.")
    private Long propertyId;

    @NotBlank(message = "Nombre obligatorio.")
    @Size(min = 2, max = 50, message = "El nombre debe contener entre 2 y 50 caracteres.")
    private String clientFirstName;

    @NotBlank(message = "Apellido obligatorio.")
    @Size(min = 2, max = 50, message = "El apellido debe contener entre 2 y 50 caracteres.")
    private String clientLastName;

    @NotBlank(message = "Email obligatorio.")
    @Email(message = "El email debe contener una direccion valida.")
    private String clientEmail;

    @ArgentinianPhoneNumber(message = "El número de teléfono debe ser un número válido de Argentina.")
    @NotBlank(message = "Numero de telefono del cliente obligatorio.")
    private String clientPhone;

    @FutureOrPresent(message = "La fecha de inicio debe ser ahora o en el futuro.")
    @NotNull(message = "Fecha de inicio requerida.")
    private LocalDateTime startDate;

    @Future(message = "La fecha de fin debe ser en el futuro.")
    @NotNull(message = "Fecha de fin requerida.")
    private LocalDateTime endDate;
}
