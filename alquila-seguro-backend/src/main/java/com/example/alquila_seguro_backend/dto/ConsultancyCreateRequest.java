package com.example.alquila_seguro_backend.dto;

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
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotBlank(message = "Details are required")
    @Size(min = 10, max = 500, message = "Details must be between 10 and 500 characters")
    private String details;
}