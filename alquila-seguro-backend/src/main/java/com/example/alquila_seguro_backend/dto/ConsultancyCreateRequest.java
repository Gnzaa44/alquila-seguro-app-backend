package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "ID del cliente obligatorio.")
    private Long clientId;

    @NotNull(message = "ID de la propiedad obligatorio.")
    private Long propertyId;

    @NotBlank(message = "Los detalles de la consulta son obligatorios.")
    @Size(min = 10, max = 500, message = "El mensaje debe contener entre 10 y 500 caracteres.")
    private String details;

}