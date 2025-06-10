package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ContractResponse;
import com.example.alquila_seguro_backend.dto.CreateContractRequest;
import com.example.alquila_seguro_backend.entity.Contract;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.repositories.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ContractService contractService;

    private Contract contract;
    private Reservation reservation;
    private CreateContractRequest createContractRequest;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .id(100L)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                // Propiedades mínimas para evitar NPE si se acceden
                .property(null)
                .client(null)
                .build();

        contract = Contract.builder()
                .id(1L)
                .reservation(reservation)
                .filePath("/docs/contract_test.pdf")
                .status(DocumentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        createContractRequest = CreateContractRequest.builder()
                .reservationId(100L)
                .filePath("/docs/new_contract.pdf")
                .build();
    }

    @Test
    @DisplayName("Test createContract should create contract successfully")
    void testCreateContract_shouldCreateContractSuccessfully() {
        // Given
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        // When
        ApiResponse<ContractResponse> response = contractService.createContract(createContractRequest);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contrato creado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getReservationId()).isEqualTo(createContractRequest.getReservationId());
        assertThat(response.getData().getFilePath()).isEqualTo(contract.getFilePath());
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test updateContractStatus should update status successfully")
    void testUpdateContractStatus_shouldUpdateStatusSuccessfully() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        Contract updatedContractEntity = Contract.builder()
                .id(1L)
                .reservation(reservation)
                .filePath("/docs/contract_test.pdf")
                .status(DocumentStatus.SENT) // Nuevo estado
                .createdAt(contract.getCreatedAt())
                .build();
        when(contractRepository.save(any(Contract.class))).thenReturn(updatedContractEntity);

        // When
        ApiResponse<ContractResponse> response = contractService.updateContractStatus(1L, DocumentStatus.SENT);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Estado del contrato actualizado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        // Verifica que el estado en la respuesta sea el nuevo estado
        assertThat(contract.getStatus()).isEqualTo(DocumentStatus.SENT); // El objeto `contract` mockeado también se actualiza
        assertThat(response.getData().getReservationId()).isEqualTo(reservation.getId());
        verify(contractRepository, times(1)).findById(1L);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test updateContractStatus should return not found when contract does not exist")
    void testUpdateContractStatus_shouldReturnNotFound_whenContractDoesNotExist() {
        // Given
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<ContractResponse> response = contractService.updateContractStatus(99L, DocumentStatus.SENT);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Contrato con el id: 99 no encontrado.");
        assertThat(response.getData()).isNull();
        verify(contractRepository, times(1)).findById(99L);
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test getContractByReservationId should return contract by reservation ID")
    void testGetContractByReservationId_shouldReturnContractByReservationId() {
        // Given
        when(contractRepository.findByReservationId(100L)).thenReturn(Optional.of(contract));

        // When
        ApiResponse<ContractResponse> response = contractService.getContractByReservationId(100L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contrato obtenido correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getReservationId()).isEqualTo(100L);
        verify(contractRepository, times(1)).findByReservationId(100L);
    }

    @Test
    @DisplayName("Test getContractByReservationId should return not found when reservation ID does not exist")
    void testGetContractByReservationId_shouldReturnNotFound_whenReservationIdDoesNotExist() {
        // Given
        when(contractRepository.findByReservationId(999L)).thenReturn(Optional.empty());

        // When
        ApiResponse<ContractResponse> response = contractService.getContractByReservationId(999L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Contrato para la reserva de id: 999 no encontrado.");
        assertThat(response.getData()).isNull();
        verify(contractRepository, times(1)).findByReservationId(999L);
    }

    @Test
    @DisplayName("Test getContractsByStatus should return list of contracts by status")
    void testGetContractsByStatus_shouldReturnListOfContractsByStatus() {
        // Given
        List<Contract> contracts = Arrays.asList(contract,
                Contract.builder()
                        .id(2L)
                        .reservation(Reservation.builder().id(101L).build())
                        .filePath("/docs/contract_2.pdf")
                        .status(DocumentStatus.PENDING)
                        .createdAt(LocalDateTime.now().plusHours(1))
                        .build());
        when(contractRepository.findByStatus(DocumentStatus.PENDING)).thenReturn(contracts);

        // When
        ApiResponse<List<ContractResponse>> response = contractService.getContractsByStatus(DocumentStatus.PENDING);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contratos obtenidos por estado correctamente.");
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getFilePath()).isEqualTo("/docs/contract_test.pdf");
        verify(contractRepository, times(1)).findByStatus(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("Test getContractsByStatus should return empty list when no contracts found for status")
    void testGetContractsByStatus_shouldReturnEmptyList_whenNoContractsFoundForStatus() {
        // Given
        when(contractRepository.findByStatus(DocumentStatus.SENT)).thenReturn(Collections.emptyList());

        // When
        ApiResponse<List<ContractResponse>> response = contractService.getContractsByStatus(DocumentStatus.SENT);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contratos obtenidos por estado correctamente.");
        assertThat(response.getData()).isEmpty();
        verify(contractRepository, times(1)).findByStatus(DocumentStatus.SENT);
    }

    @Test
    @DisplayName("Test getContractById should return contract by ID")
    void testGetContractById_shouldReturnContractById() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        // When
        ApiResponse<ContractResponse> response = contractService.getContractById(1L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contrato con el id: 1 encontrado.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        verify(contractRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getContractById should return not found when ID does not exist")
    void testGetContractById_shouldReturnNotFound_whenIdDoesNotExist() {
        // Given
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<ContractResponse> response = contractService.getContractById(99L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Contrato con el id: 99 no encontrado.");
        assertThat(response.getData()).isNull();
        verify(contractRepository, times(1)).findById(99L);
    }
}