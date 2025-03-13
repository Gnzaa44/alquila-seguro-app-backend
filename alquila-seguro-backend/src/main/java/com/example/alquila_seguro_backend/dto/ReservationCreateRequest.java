package com.example.alquila_seguro_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
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
public class ReservationCreateRequest {
    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @FutureOrPresent(message = "Start date must be today or in the future")
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
}
