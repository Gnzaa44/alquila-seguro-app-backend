package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.controller.ContractController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ContractResponse;
import com.example.alquila_seguro_backend.dto.CreateContractRequest;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.security.config.SecurityConfig;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthEntryPoint;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthFilter;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;
import com.example.alquila_seguro_backend.services.ContractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
@Import({
        SecurityConfig.class,
        JwtAuthEntryPoint.class,
        JwtAuthFilter.class,
        JwtUtils.class
})
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContractService contractService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl; // Necesario para la seguridad

    @Autowired
    private ObjectMapper objectMapper;

    private CreateContractRequest createContractRequest;
    private ContractResponse contractResponse;
    private ApiResponse<ContractResponse> successResponse;
    private ApiResponse<ContractResponse> notFoundResponse;
    private ApiResponse<List<ContractResponse>> successListResponse;

    @BeforeEach
    void setUp() {
        createContractRequest = CreateContractRequest.builder()
                .reservationId(1L)
                .filePath("/docs/test_contract.pdf")
                .build();

        contractResponse = ContractResponse.builder()
                .id(1L)
                .reservationId(1L)
                .createdAt(LocalDateTime.now())
                .filePath("/docs/test_contract.pdf")
                .build();

        successResponse = ApiResponse.<ContractResponse>builder()
                .success(true)
                .message("Éxito")
                .data(contractResponse)
                .build();

        notFoundResponse = ApiResponse.<ContractResponse>builder()
                .success(false)
                .message("Contrato no encontrado")
                .data(null)
                .build();

        successListResponse = ApiResponse.<List<ContractResponse>>builder()
                .success(true)
                .message("Contratos obtenidos correctamente.")
                .data(Collections.singletonList(contractResponse))
                .build();
    }

    // --- Endpoints públicos (sin autenticación requerida) ---

    @Test
    @DisplayName("GET /alquila-seg/contracts/reservation/{id} should return contract by reservation id for unauthenticated user")
    void getContractByReservationId_shouldReturnContract_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractByReservationId(1L)).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/reservation/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(contractService, times(1)).getContractByReservationId(1L);
    }

    @Test
    @DisplayName("GET /alquila-seg/contracts/reservation/{id} should return not found for non-existent reservation id (unauthenticated)")
    void getContractByReservationId_shouldReturnNotFound_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractByReservationId(99L)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/reservation/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contrato no encontrado"));

        verify(contractService, times(1)).getContractByReservationId(99L);
    }

    @Test
    @DisplayName("GET /alquila-seg/contracts/status/{status} should return contracts by status for unauthenticated user")
    void getContractsByStatus_shouldReturnContracts_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractsByStatus(DocumentStatus.PENDING)).thenReturn(successListResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/status/{status}", DocumentStatus.PENDING)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(contractService, times(1)).getContractsByStatus(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("GET /alquila-seg/contracts/status/{status} should return empty list for no contracts (unauthenticated)")
    void getContractsByStatus_shouldReturnEmptyList_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractsByStatus(DocumentStatus.SENT)).thenReturn(ApiResponse.<List<ContractResponse>>builder()
                .success(true)
                .message("Contratos obtenidos por estado correctamente.")
                .data(Collections.emptyList())
                .build());

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/status/{status}", DocumentStatus.SENT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(contractService, times(1)).getContractsByStatus(DocumentStatus.SENT);
    }

    @Test
    @DisplayName("GET /alquila-seg/contracts/{id} should return contract by ID for unauthenticated user")
    void getContractById_shouldReturnContract_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractById(1L)).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(contractService, times(1)).getContractById(1L);
    }

    @Test
    @DisplayName("GET /alquila-seg/contracts/{id} should return not found for non-existent ID (unauthenticated)")
    void getContractById_shouldReturnNotFound_unauthenticated() throws Exception {
        // Given
        when(contractService.getContractById(99L)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/contracts/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contrato no encontrado"));

        verify(contractService, times(1)).getContractById(99L);
    }

    // --- Endpoints protegidos (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /alquila-seg/contracts should create a new contract (ADMIN)")
    void createContract_shouldCreateNewContract_admin() throws Exception {
        // Given
        when(contractService.createContract(any(CreateContractRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/alquila-seg/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContractRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(contractService, times(1)).createContract(any(CreateContractRequest.class));
    }

    @Test
    @DisplayName("POST /alquila-seg/contracts should return unauthorized for unauthenticated user")
    void createContract_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/alquila-seg/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContractRequest)))
                .andExpect(status().isUnauthorized());
        verify(contractService, never()).createContract(any(CreateContractRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /alquila-seg/contracts should return bad request for invalid input (ADMIN)")
    void createContract_shouldReturnBadRequest_forInvalidInput_admin() throws Exception {
        // Given - Petición inválida (reservationId nulo)
        CreateContractRequest invalidRequest = CreateContractRequest.builder()
                .reservationId(null) // Inválido
                .filePath("/docs/invalid.pdf")
                .build();

        // When & Then
        mockMvc.perform(post("/alquila-seg/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        verify(contractService, never()).createContract(any(CreateContractRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/contracts/{id}/status should update contract status (ADMIN)")
    void updateContractStatus_shouldUpdateStatus_admin() throws Exception {
        // Given
        ApiResponse<ContractResponse> updatedSuccessResponse = ApiResponse.<ContractResponse>builder()
                .success(true)
                .message("Estado del contrato actualizado correctamente.")
                .data(ContractResponse.builder()
                        .id(1L)
                        .reservationId(1L)
                        .filePath("/docs/test_contract.pdf")
                        .createdAt(LocalDateTime.now())
                        .build())
                .build();
        when(contractService.updateContractStatus(eq(1L), eq(DocumentStatus.SENT))).thenReturn(updatedSuccessResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/contracts/{id}/status", 1L)
                        .with(csrf())
                        .param("status", DocumentStatus.SENT.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado del contrato actualizado correctamente."));

        verify(contractService, times(1)).updateContractStatus(eq(1L), eq(DocumentStatus.SENT));
    }

    @Test
    @DisplayName("PUT /alquila-seg/contracts/{id}/status should return unauthorized for unauthenticated user")
    void updateContractStatus_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(put("/alquila-seg/contracts/{id}/status", 1L)
                        .with(csrf())
                        .param("status", DocumentStatus.SENT.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(contractService, never()).updateContractStatus(anyLong(), any(DocumentStatus.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/contracts/{id}/status should return not found for non-existent id (ADMIN)")
    void updateContractStatus_shouldReturnNotFound_admin() throws Exception {
        // Given
        when(contractService.updateContractStatus(eq(99L), eq(DocumentStatus.ERROR))).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/contracts/{id}/status", 99L)
                        .with(csrf())
                        .param("status", DocumentStatus.ERROR.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contrato no encontrado"));

        verify(contractService, times(1)).updateContractStatus(eq(99L), eq(DocumentStatus.ERROR));
    }
}