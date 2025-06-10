package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.controller.InvoiceController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.CreateInvoiceRequest;
import com.example.alquila_seguro_backend.dto.InvoiceResponse;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.security.config.SecurityConfig;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthEntryPoint;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthFilter;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;
import com.example.alquila_seguro_backend.services.InvoiceService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@Import({
        SecurityConfig.class,
        JwtAuthEntryPoint.class,
        JwtAuthFilter.class,
        JwtUtils.class
})
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl; // Necesario para la seguridad

    @Autowired
    private ObjectMapper objectMapper;

    private CreateInvoiceRequest createInvoiceRequest;
    private InvoiceResponse invoiceResponse;
    private ApiResponse<InvoiceResponse> successResponse;
    private ApiResponse<InvoiceResponse> notFoundResponse;
    private ApiResponse<List<InvoiceResponse>> successListResponse;

    @BeforeEach
    void setUp() {
        createInvoiceRequest = CreateInvoiceRequest.builder()
                .reservationId(1L)
                .totalAmount(new BigDecimal("750.00"))
                .filePath("/docs/test_invoice.pdf")
                .build();

        invoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .reservationId(1L)
                .totalAmount(new BigDecimal("700.00"))
                .filePath("/docs/test_invoice.pdf")
                .issuedAt(LocalDateTime.now())
                .build();

        successResponse = ApiResponse.<InvoiceResponse>builder()
                .success(true)
                .message("Éxito")
                .data(invoiceResponse)
                .build();

        notFoundResponse = ApiResponse.<InvoiceResponse>builder()
                .success(false)
                .message("Factura no encontrada")
                .data(null)
                .build();

        successListResponse = ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Facturas obtenidas correctamente.")
                .data(Collections.singletonList(invoiceResponse))
                .build();
    }

    // --- Endpoints públicos (sin autenticación requerida) ---

    @Test
    @DisplayName("GET /alquila-seg/invoices/{id} should return invoice by ID for unauthenticated user")
    void getInvoiceById_shouldReturnInvoice_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoiceById(1L)).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(invoiceService, times(1)).getInvoiceById(1L);
    }

    @Test
    @DisplayName("GET /alquila-seg/invoices/{id} should return not found for non-existent ID (unauthenticated)")
    void getInvoiceById_shouldReturnNotFound_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoiceById(99L)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Factura no encontrada"));

        verify(invoiceService, times(1)).getInvoiceById(99L);
    }

    @Test
    @DisplayName("GET /alquila-seg/invoices/reservation/{id} should return invoice by reservation id for unauthenticated user")
    void getInvoiceByReservationId_shouldReturnInvoice_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoiceByReservationId(1L)).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/reservation/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(invoiceService, times(1)).getInvoiceByReservationId(1L);
    }

    @Test
    @DisplayName("GET /alquila-seg/invoices/reservation/{id} should return not found for non-existent reservation id (unauthenticated)")
    void getInvoiceByReservationId_shouldReturnNotFound_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoiceByReservationId(99L)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/reservation/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Factura no encontrada")); // Adjusted message

        verify(invoiceService, times(1)).getInvoiceByReservationId(99L);
    }

    @Test
    @DisplayName("GET /alquila-seg/invoices/status/{status} should return invoices by status for unauthenticated user")
    void getInvoicesByStatus_shouldReturnInvoices_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoicesByStatus(DocumentStatus.PENDING)).thenReturn(successListResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/status/{status}", DocumentStatus.PENDING)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(invoiceService, times(1)).getInvoicesByStatus(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("GET /alquila-seg/invoices/status/{status} should return empty list for no invoices (unauthenticated)")
    void getInvoicesByStatus_shouldReturnEmptyList_unauthenticated() throws Exception {
        // Given
        when(invoiceService.getInvoicesByStatus(DocumentStatus.SENT)).thenReturn(ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Facturas obtenidas correctamente.") // Adjusted message
                .data(Collections.emptyList())
                .build());

        // When & Then
        mockMvc.perform(get("/alquila-seg/invoices/status/{status}", DocumentStatus.SENT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(invoiceService, times(1)).getInvoicesByStatus(DocumentStatus.SENT);
    }

    // --- Endpoints protegidos (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /alquila-seg/invoices should create a new invoice (ADMIN)")
    void createInvoice_shouldCreateNewInvoice_admin() throws Exception {
        // Given
        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/alquila-seg/invoices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInvoiceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(invoiceService, times(1)).createInvoice(any(CreateInvoiceRequest.class));
    }

    @Test
    @DisplayName("POST /alquila-seg/invoices should return unauthorized for unauthenticated user")
    void createInvoice_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/alquila-seg/invoices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInvoiceRequest)))
                .andExpect(status().isUnauthorized());
        verify(invoiceService, never()).createInvoice(any(CreateInvoiceRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /alquila-seg/invoices should return bad request for invalid input (ADMIN)")
    void createInvoice_shouldReturnBadRequest_forInvalidInput_admin() throws Exception {
        // Given - Petición inválida (totalAmount < 0.01)
        CreateInvoiceRequest invalidRequest = CreateInvoiceRequest.builder()
                .reservationId(1L)
                .totalAmount(BigDecimal.ZERO) // Inválido
                .filePath("/docs/invalid.pdf")
                .build();

        // When & Then
        mockMvc.perform(post("/alquila-seg/invoices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)); // Should return false for validation errors
        verify(invoiceService, never()).createInvoice(any(CreateInvoiceRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/invoices/{id}/status should update invoice status (ADMIN)")
    void updateInvoiceStatus_shouldUpdateStatus_admin() throws Exception {
        // Given
        ApiResponse<InvoiceResponse> updatedSuccessResponse = ApiResponse.<InvoiceResponse>builder()
                .success(true)
                .message("Estado de la factura actualizado correctamente.")
                .data(InvoiceResponse.builder()
                        .id(1L)
                        .reservationId(1L)
                        .totalAmount(new BigDecimal("700.00"))
                        .filePath("/docs/test_invoice.pdf")
                        .issuedAt(LocalDateTime.now())
                        .build())
                .build();
        when(invoiceService.updateInvoiceStatus(eq(1L), eq(DocumentStatus.SENT))).thenReturn(updatedSuccessResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/invoices/{id}/status", 1L)
                        .with(csrf())
                        .param("status", DocumentStatus.SENT.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado de la factura actualizado correctamente."));

        verify(invoiceService, times(1)).updateInvoiceStatus(eq(1L), eq(DocumentStatus.SENT));
    }

    @Test
    @DisplayName("PUT /alquila-seg/invoices/{id}/status should return unauthorized for unauthenticated user")
    void updateInvoiceStatus_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(put("/alquila-seg/invoices/{id}/status", 1L)
                        .with(csrf())
                        .param("status", DocumentStatus.SENT.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(invoiceService, never()).updateInvoiceStatus(anyLong(), any(DocumentStatus.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/invoices/{id}/status should return not found for non-existent id (ADMIN)")
    void updateInvoiceStatus_shouldReturnNotFound_admin() throws Exception {
        // Given
        when(invoiceService.updateInvoiceStatus(eq(99L), eq(DocumentStatus.ERROR))).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/invoices/{id}/status", 99L)
                        .with(csrf())
                        .param("status", DocumentStatus.ERROR.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Factura no encontrada"));

        verify(invoiceService, times(1)).updateInvoiceStatus(eq(99L), eq(DocumentStatus.ERROR));
    }
}