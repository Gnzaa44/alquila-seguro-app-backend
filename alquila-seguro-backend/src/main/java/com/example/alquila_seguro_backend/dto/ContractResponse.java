package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private Long id;
    private Long reservationId;
    private LocalDateTime createdAt;
    private DocumentStatus status;
}
