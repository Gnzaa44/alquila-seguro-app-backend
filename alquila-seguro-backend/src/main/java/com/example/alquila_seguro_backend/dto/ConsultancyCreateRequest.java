package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.validation.ArgentinianPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultancyCreateRequest {
    @NotBlank(message = "Nombre obligatorio.")
    @Size(min = 2, max = 50, message = "El nombre del cliente debe contener entre 2  y 50 caracteres.")
    private String clientFirstName;

    @NotBlank(message = "Apellido obligatorio.")
    @Size(min = 2, max = 50, message = "El apellido del cliente debe contener entre 2  y 50 caracteres.")
    private String clientLastName;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email del cliente debe ser una direccion de correo valida.")
    private String clientEmail;

    @ArgentinianPhoneNumber(message = "El número de teléfono debe ser un número válido de Argentina.")
    @NotBlank(message = "Numero de teléfono obligatorio.")
    private String clientPhone;

    @NotBlank(message = "Los detalles de la consulta son obligatorios.")
    @Size(min = 10, max = 500, message = "El mensaje debe contener entre 10 y 500 caracteres.")
    private String details;

}