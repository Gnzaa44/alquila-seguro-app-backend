package com.example.alquila_seguro_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceResponse {
    private String preferenceId;
    private String initPoint;
}
