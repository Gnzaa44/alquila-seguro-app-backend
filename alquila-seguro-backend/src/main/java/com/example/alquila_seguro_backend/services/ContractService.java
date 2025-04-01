package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ContractResponse;
import com.example.alquila_seguro_backend.dto.CreateContractRequest;
import com.example.alquila_seguro_backend.entity.Contract;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;

    private ContractResponse mapToContractResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .reservationId(contract.getReservation().getId())
                .createdAt(contract.getCreatedAt())
                .filePath(contract.getFilePath())
                .build();

    }
    @Transactional
    public ApiResponse<ContractResponse> createContract(CreateContractRequest request) {
        Contract contract = Contract.builder()
                .reservation(Reservation.builder().id(request.getReservationId()).build())
                .filePath(request.getFilePath())
                .status(DocumentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Contract savedContract = contractRepository.save(contract);
        return ApiResponse.<ContractResponse>builder()
                .success(true)
                .message("Contrato creado correctamente.")
                .data(mapToContractResponse(savedContract))
                .build();

    }
    @Transactional
    public ApiResponse<ContractResponse> updateContractStatus(Long id, DocumentStatus status) {
        return contractRepository.findById(id)
                .map(contract -> {
                    contract.setStatus(status);
                    Contract updatedContract = contractRepository.save(contract);
                    return ApiResponse.<ContractResponse>builder()
                            .success(true)
                            .message("Estado del contrato actualizado correctamente.")
                            .data(mapToContractResponse(updatedContract))
                            .build();
                })
                .orElse(ApiResponse.<ContractResponse>builder()
                        .success(false)
                        .message("Contrato con el id: " + id + " no encontrado.")
                        .build());
    }

    public ApiResponse<ContractResponse> getContractByReservationId(Long reservationId) {
        return contractRepository.findByReservationId(reservationId)
                .map(contract -> ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contrato obtenido correctamente.")
                        .data(mapToContractResponse(contract))
                        .build())
                .orElse(ApiResponse.<ContractResponse>builder()
                        .success(false)
                        .message("Contrato para la reserva de id: " + reservationId + " no encontrado.")
                        .build());
    }

    public ApiResponse<List<ContractResponse>> getContractsByStatus(DocumentStatus status) {
        List<ContractResponse> contracts = contractRepository.findByStatus(status).stream()
                .map(this::mapToContractResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ContractResponse>>builder()
                .success(true)
                .message("Contratos obtenidos por estado correctamente.")
                .data(contracts)
                .build();
    }

    public ApiResponse<ContractResponse> getContractById(Long id) {
        return contractRepository.findById(id)
                .map(contract -> ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contrato con el id: " + id + " encontrado.")
                        .data(mapToContractResponse(contract))
                        .build())
                .orElse(ApiResponse.<ContractResponse>builder()
                        .success(false)
                        .message("Contrato con el id: " + id + " no encontrado.")
                        .build());
    }

}
