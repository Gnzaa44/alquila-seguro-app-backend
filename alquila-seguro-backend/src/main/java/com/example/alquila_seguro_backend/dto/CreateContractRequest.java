package com.example.alquila_seguro_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContractRequest {
    @NotNull(message = "El id de la reserva es requerido.")
    private Long reservationId;
    @NotNull(message = "La ruta del archivo es requerida.")
    private String filePath;
}
