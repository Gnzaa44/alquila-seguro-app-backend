package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.controller.ClientController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientCreateRequest;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.security.config.SecurityConfig;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthEntryPoint;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthFilter;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;
import com.example.alquila_seguro_backend.services.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Todavía necesaria para ADMIN
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import({
        SecurityConfig.class,
        JwtAuthEntryPoint.class,
        JwtAuthFilter.class,
        JwtUtils.class
})
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    // Dependencias de seguridad que necesitarás mockear como hicimos con PropertyControllerTest
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientCreateRequest clientCreateRequest;
    private ClientResponse clientResponse;
    private ApiResponse<ClientResponse> successResponse;
    private ApiResponse<ClientResponse> notFoundResponse;
    private ApiResponse<List<ClientResponse>> successListResponse;
    private ApiResponse<Void> successVoidResponse;

    @BeforeEach
    void setUp() {
        clientCreateRequest = ClientCreateRequest.builder()
                .firstName("Test")
                .lastName("Client")
                .email("test.client@example.com")
                .phone("2235674523")
                .build();

        clientResponse = ClientResponse.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Client")
                .email("test.client@example.com")
                .phone("2235667788")
                .createdAt(LocalDateTime.now())
                .build();

        successResponse = ApiResponse.<ClientResponse>builder()
                .success(true)
                .message("Éxito")
                .data(clientResponse)
                .build();

        notFoundResponse = ApiResponse.<ClientResponse>builder()
                .success(false)
                .message("Cliente no encontrado")
                .data(null)
                .build();

        successListResponse = ApiResponse.<List<ClientResponse>>builder()
                .success(true)
                .message("Clientes recuperados correctamente.")
                .data(Collections.singletonList(clientResponse))
                .build();

        successVoidResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Éxito")
                .build();
    }

    // --- Tests para Endpoints Públicos (sin autenticación) ---

    @Test
    @DisplayName("GET /alquila-seg/clients should return all clients for unauthenticated user")
    void getAllClients_shouldReturnAllClients_unauthenticated() throws Exception {
        // Given
        when(clientService.getAllClients()).thenReturn(successListResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Clientes recuperados correctamente."))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    @DisplayName("GET /alquila-seg/clients/{id} should return client by id for unauthenticated user")
    void getClientById_shouldReturnClient_unauthenticated() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    @DisplayName("GET /alquila-seg/clients/{id} should return not found for non-existent id (unauthenticated)")
    void getClientById_shouldReturnNotFound_unauthenticated() throws Exception {
        // Given
        when(clientService.getClientById(99L)).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(get("/alquila-seg/clients/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));

        verify(clientService, times(1)).getClientById(99L);
    }

    // --- Tests para Endpoints Protegidos (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Simula un usuario ADMIN autenticado
    @DisplayName("POST /alquila-seg/clients should create a new client (ADMIN)")
    void createClient_shouldCreateNewClient_admin() throws Exception {
        // Given
        when(clientService.createClient(any(ClientCreateRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/alquila-seg/clients")
                        .with(csrf()) // Necesario para POST, PUT, DELETE con Spring Security
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(clientService, times(1)).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("POST /alquila-seg/clients should return unauthorized for unauthenticated user")
    void createClient_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/alquila-seg/clients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
        verify(clientService, never()).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /alquila-seg/clients should return bad request for invalid input (ADMIN)")
    void createClient_shouldReturnBadRequest_forInvalidInput_admin() throws Exception {
        // Given - Petición inválida (firstName en blanco)
        ClientCreateRequest invalidRequest = ClientCreateRequest.builder()
                .firstName("") // Inválido
                .lastName("Client")
                .email("test.client@example.com")
                .phone("1122334455")
                .build();

        // When & Then
        mockMvc.perform(post("/alquila-seg/clients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // Espera 400 por la validación @Valid
                .andExpect(jsonPath("$.success").value(false));
        verify(clientService, never()).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/clients/{id} should update client (ADMIN)")
    void updateClient_shouldUpdateClient_admin() throws Exception {
        // Given
        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .firstName("Updated")
                .lastName("Client")
                .email("updated.client@example.com")
                .phone("2235667788")
                .build();
        when(clientService.updateClient(eq(1L), any(ClientCreateRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/clients/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Test")); // Nota: El mock devuelve "Test" del setup, no "Updated"

        verify(clientService, times(1)).updateClient(eq(1L), any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("PUT /alquila-seg/clients/{id} should return unauthorized for unauthenticated user")
    void updateClient_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(put("/alquila-seg/clients/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
        verify(clientService, never()).updateClient(anyLong(), any(ClientCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /alquila-seg/clients/{id} should return not found for non-existent id (ADMIN)")
    void updateClient_shouldReturnNotFound_forNonExistentId_admin() throws Exception {
        // Given
        when(clientService.updateClient(eq(99L), any(ClientCreateRequest.class))).thenReturn(notFoundResponse);

        // When & Then
        mockMvc.perform(put("/alquila-seg/clients/{id}", 99L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));

        verify(clientService, times(1)).updateClient(eq(99L), any(ClientCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /alquila-seg/clients/{id} should delete client (ADMIN)")
    void deleteClient_shouldDeleteClient_admin() throws Exception {
        // Given
        when(clientService.deleteClient(1L)).thenReturn(successVoidResponse);

        // When & Then
        mockMvc.perform(delete("/alquila-seg/clients/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    @DisplayName("DELETE /alquila-seg/clients/{id} should return unauthorized for unauthenticated user")
    void deleteClient_shouldReturnUnauthorized_unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/alquila-seg/clients/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
        verify(clientService, never()).deleteClient(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /alquila-seg/clients/{id} should return not found for non-existent id (ADMIN)")
    void deleteClient_shouldReturnNotFound_forNonExistentId_admin() throws Exception {
        // Given
        ApiResponse<Void> voidNotFoundResponse = ApiResponse.<Void>builder()
                .success(false)
                .message("Cliente con id: 99 no encontrado.")
                .build();
        when(clientService.deleteClient(99L)).thenReturn(voidNotFoundResponse);

        // When & Then
        mockMvc.perform(delete("/alquila-seg/clients/{id}", 99L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cliente con id: 99 no encontrado."));

        verify(clientService, times(1)).deleteClient(99L);
    }
}