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
public class ClientCreateRequest {
    @NotBlank(message = "Nombre obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe contener entre 3 y 100 caracteres.")
    private String firstName;

    @NotBlank(message = "Apellido obligatorio.")
    @Size(min = 3, max = 100, message = "El apellido debe contener entre 3 y 100 caracteres.")
    private String lastName;

    @Email(message = "Email obligatorio.")
    @NotBlank(message = "Email requerido. ")
    private String email;

    @ArgentinianPhoneNumber(message = "El telefono debe ser un numero de Argentina valido.")
    @NotBlank(message = "Numero obligatorio.")
    @Size(min = 10, max = 15, message = "El telefono debe contener entre 10 y 15 numeros.")
    private String phone;
}
