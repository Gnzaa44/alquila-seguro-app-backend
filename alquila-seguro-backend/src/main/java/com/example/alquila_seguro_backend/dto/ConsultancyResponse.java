package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultancyResponse {
    private Long id;
    private ClientResponse client;
    private String details;
    private LocalDateTime requestedAt;
    private BigDecimal totalAmount;
    private ConsultancyStatus status;
}
