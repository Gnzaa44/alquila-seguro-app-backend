package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private Long reservationId;
    private BigDecimal totalAmount;
    private LocalDateTime issuedAt;
    private DocumentStatus status;
}
