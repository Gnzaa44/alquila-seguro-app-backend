package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private PropertyResponse property;
    private ClientResponse client;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean hasInvoice;
    private boolean hasContract;
}
