package com.example.alquila_seguro_backend.dto;

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
public class ConsultancyResponseRequest {
    @NotBlank(message = "Response is required")
    @Size(min = 10, max = 500, message = "Response must be between 10 and 500 characters")
    private String response;
}






