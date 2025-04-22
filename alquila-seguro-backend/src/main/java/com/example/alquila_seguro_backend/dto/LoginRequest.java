package com.example.alquila_seguro_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Nombre de usuario requerido.")
    private String username;

    @NotBlank(message = "Contraseña requerida.")
    private String password;
}