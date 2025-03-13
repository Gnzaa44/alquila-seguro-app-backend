package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultancyResponse {
    private Long id;
    private ClientResponse client;
    private PropertyResponse property;
    private String details;
    private String response;
    private LocalDateTime requestedAt;
    private ConsultancyStatus status;
}
