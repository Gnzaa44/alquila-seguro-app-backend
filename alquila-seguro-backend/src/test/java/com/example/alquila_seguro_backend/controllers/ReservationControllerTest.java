package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.controller.ReservationController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.dto.ReservationCreateRequest;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.entity.ReservationStatus;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.services.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtUtils jwtUtils; // Necesario para @WebMvcTest con Spring Security

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationResponse testReservationResponse;
    private ReservationCreateRequest testReservationCreateRequest;
    private PropertyResponse testPropertyResponse;
    private ClientResponse testClientResponse;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDateTime en JSON

        testClientResponse = ClientResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("2234567812")
                .createdAt(LocalDateTime.now())
                .build();

        testPropertyResponse = PropertyResponse.builder()
                .id(1L)
                .title("Casa de prueba")
                .pricePerNight(500.0)
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();

        testReservationResponse = ReservationResponse.builder()
                .id(1L)
                .property(testPropertyResponse)
                .client(testClientResponse)
                .startDate(LocalDateTime.of(2025, 7, 10, 10, 0))
                .endDate(LocalDateTime.of(2025, 7, 15, 10, 0))
                .hasInvoice(true)
                .hasContract(true)
                .totalAmount(BigDecimal.valueOf(2500.0)) // 5 días * 500
                .build();

        testReservationCreateRequest = ReservationCreateRequest.builder()
                .propertyId(1L)
                .clientFirstName("Nuevo")
                .clientLastName("Cliente")
                .clientEmail("nuevo.cliente@example.com")
                .clientPhone("2234890043")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();
    }

    // --- Tests para getAllReservations ---
    @Test
    @WithMockUser
    void testGetAllReservations_Success() throws Exception {
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas recuperadas correctamente.", Arrays.asList(testReservationResponse));
        when(reservationService.getAllReservations()).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(testReservationResponse.getId()));

        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    @WithMockUser
    void testGetAllReservations_NoContent() throws Exception {
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas recuperadas correctamente.", Collections.emptyList());
        when(reservationService.getAllReservations()).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(reservationService, times(1)).getAllReservations();
    }

    // --- Tests para getReservationById ---
    @Test
    @WithMockUser
    void testGetReservationById_Success() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true, "Reserva obtenida correctamente.", testReservationResponse);
        when(reservationService.getReservationById(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/{id}", testReservationResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reserva obtenida correctamente."))
                .andExpect(jsonPath("$.data.id").value(testReservationResponse.getId()));

        verify(reservationService, times(1)).getReservationById(testReservationResponse.getId());
    }

    @Test
    @WithMockUser
    void testGetReservationById_NotFound() throws Exception {
        Long nonExistentId = 99L;
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Reserva con el id: " + nonExistentId + " no encontrada.", null);
        when(reservationService.getReservationById(nonExistentId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // El servicio devuelve OK con success=false
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reserva con el id: " + nonExistentId + " no encontrada."));

        verify(reservationService, times(1)).getReservationById(nonExistentId);
    }

    // Nota: Si quieres que getReservationById lance una EntityNotFoundException
    // y tu GlobalExceptionHandler la capture y devuelva un 404, tendrías que:
    // 1. Modificar el ReservationService.getReservationById para lanzar EntityNotFoundException.
    // 2. Mockear esa excepción aquí: .thenThrow(new EntityNotFoundException("..."));
    // 3. Esperar status().isNotFound() y un cuerpo JSON que coincida con tu GlobalExceptionHandler.
    // Por ahora, tu servicio devuelve un ApiResponse<false>, lo cual resulta en un 200 OK en el controlador.


    // --- Tests para getReservationsByClientId ---
    @Test
    @WithMockUser
    void testGetReservationsByClientId_Success() throws Exception {
        Long clientId = 1L;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por cliente recuperadas correctamente.", Arrays.asList(testReservationResponse));
        when(reservationService.getReservationsByClientId(clientId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/client/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por cliente recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(reservationService, times(1)).getReservationsByClientId(clientId);
    }

    @Test
    @WithMockUser
    void testGetReservationsByClientId_NoContent() throws Exception {
        Long clientId = 99L;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por cliente recuperadas correctamente.", Collections.emptyList());
        when(reservationService.getReservationsByClientId(clientId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/client/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por cliente recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(reservationService, times(1)).getReservationsByClientId(clientId);
    }

    // --- Tests para getReservationsByPropertyId ---
    @Test
    @WithMockUser
    void testGetReservationsByPropertyId_Success() throws Exception {
        Long propertyId = 1L;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por propiedad recuperadas correctamente.", Arrays.asList(testReservationResponse));
        when(reservationService.getReservationsByPropertyId(propertyId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/property/{propertyId}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por propiedad recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(reservationService, times(1)).getReservationsByPropertyId(propertyId);
    }

    @Test
    @WithMockUser
    void testGetReservationsByPropertyId_NoContent() throws Exception {
        Long propertyId = 99L;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por propiedad recuperadas correctamente.", Collections.emptyList());
        when(reservationService.getReservationsByPropertyId(propertyId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/property/{propertyId}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por propiedad recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(reservationService, times(1)).getReservationsByPropertyId(propertyId);
    }

    // --- Tests para getReservationsByStatusId ---
    @Test
    @WithMockUser
    void testGetReservationsByStatusId_Success() throws Exception {
        ReservationStatus status = ReservationStatus.PENDING;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por estado recuperadas correctamente.", Arrays.asList(testReservationResponse));
        when(reservationService.getReservationsByStatus(status)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/status/{statusId}", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por estado recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(reservationService, times(1)).getReservationsByStatus(status);
    }

    @Test
    @WithMockUser
    void testGetReservationsByStatusId_NoContent() throws Exception {
        ReservationStatus status = ReservationStatus.COMPLETED;
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>(
                true, "Reservas por estado recuperadas correctamente.", Collections.emptyList());
        when(reservationService.getReservationsByStatus(status)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/reservations/status/{statusId}", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservas por estado recuperadas correctamente."))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(reservationService, times(1)).getReservationsByStatus(status);
    }

    // --- Tests para createReservation ---
    @Test
    @WithMockUser
    void testCreateReservation_Success() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true, "Reserva creada correctamente.", testReservationResponse);
        when(reservationService.createReservation(any(ReservationCreateRequest.class))).thenReturn(apiResponse);

        mockMvc.perform(post("/alquila-seg/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservationCreateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reserva creada correctamente."))
                .andExpect(jsonPath("$.data.id").value(testReservationResponse.getId()));

        verify(reservationService, times(1)).createReservation(any(ReservationCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateReservation_BadRequest_ValidationErrors() throws Exception {
        ReservationCreateRequest invalidRequest = ReservationCreateRequest.builder()
                .propertyId(null) // Null
                .clientFirstName("A") // Too short
                .clientLastName("") // Blank
                .clientEmail("invalid") // Invalid email
                .clientPhone("123") // Invalid phone
                .startDate(LocalDateTime.now().plusDays(5)) // Start after end if end is past
                .endDate(LocalDateTime.now().minusDays(1)) // Past date
                .build();

        // No need to mock service for validation errors, Spring's @Valid handles it.

        mockMvc.perform(post("/alquila-seg/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest()) // Expected 400 Bad Request
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error de validación."))
                .andExpect(jsonPath("$.data.propertyId").value("El ID de la propiedad es obligatorio."))
                .andExpect(jsonPath("$.data.clientFirstName").value("El nombre debe contener entre 2 y 50 caracteres."))
                .andExpect(jsonPath("$.data.clientLastName").value("Apellido obligatorio."))
                .andExpect(jsonPath("$.data.clientEmail").value("El email debe contener una direccion valida."))
                .andExpect(jsonPath("$.data.clientPhone").value("El número de teléfono debe ser un número válido de Argentina."))
                .andExpect(jsonPath("$.data.endDate").value("La fecha de fin debe ser en el futuro."));

        verify(reservationService, never()).createReservation(any(ReservationCreateRequest.class)); // Service should not be called
    }

    @Test
    @WithMockUser
    void testCreateReservation_ServiceReturnsFailure() throws Exception {
        // Simulate a business logic failure from the service (e.g., property not available)
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Propiedad no disponible para la reserva.", null);
        when(reservationService.createReservation(any(ReservationCreateRequest.class))).thenReturn(apiResponse);

        mockMvc.perform(post("/alquila-seg/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservationCreateRequest))
                        .with(csrf()))
                .andExpect(status().isOk()) // Service returns success=false, controller returns 200 OK
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Propiedad no disponible para la reserva."));

        verify(reservationService, times(1)).createReservation(any(ReservationCreateRequest.class));
    }


    // --- Tests para confirmReservation ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void testConfirmReservation_Success() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true, "Reserva confirmada.", testReservationResponse);
        when(reservationService.confirmReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/confirm", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reserva confirmada."));

        verify(reservationService, times(1)).confirmReservation(testReservationResponse.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testConfirmReservation_NotFound() throws Exception {
        Long nonExistentId = 99L;
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Reserva con el id: " + nonExistentId + " no encontrada.", null);
        when(reservationService.confirmReservation(nonExistentId)).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/confirm", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isOk()) // Service returns success=false, controller returns 200 OK
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reserva con el id: " + nonExistentId + " no encontrada."));

        verify(reservationService, times(1)).confirmReservation(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testConfirmReservation_NotPending() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Solo las reservas pendientes pueden ser confirmadas.", null);
        when(reservationService.confirmReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/confirm", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk()) // Service returns success=false, controller returns 200 OK
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Solo las reservas pendientes pueden ser confirmadas."));

        verify(reservationService, times(1)).confirmReservation(testReservationResponse.getId());
    }

    @Test
    @WithAnonymousUser
    void testConfirmReservation_UnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(put("/alquila-seg/reservations/{id}/confirm", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized()); // Forbidden for non-ADMIN users
        verify(reservationService, never()).confirmReservation(anyLong());
    }

    // --- Tests para cancelReservation ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCancelReservation_Success() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true, "Reserva cancelada correctamente.", testReservationResponse);
        when(reservationService.cancelReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/cancel", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reserva cancelada correctamente."));

        verify(reservationService, times(1)).cancelReservation(testReservationResponse.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCancelReservation_NotFound() throws Exception {
        Long nonExistentId = 99L;
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Reserva con el id: " + nonExistentId + " no encontrada.", null);
        when(reservationService.cancelReservation(nonExistentId)).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/cancel", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reserva con el id: " + nonExistentId + " no encontrada."));

        verify(reservationService, times(1)).cancelReservation(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCancelReservation_CannotCancel() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "No se puede cancelar una reserva ya completada o cancelada.", null);
        when(reservationService.cancelReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/cancel", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No se puede cancelar una reserva ya completada o cancelada."));

        verify(reservationService, times(1)).cancelReservation(testReservationResponse.getId());
    }

    @Test
    @WithAnonymousUser
    void testCancelReservation_UnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(put("/alquila-seg/reservations/{id}/cancel", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
        verify(reservationService, never()).cancelReservation(anyLong());
    }

    // --- Tests para completeReservation ---
    @Test
    @WithMockUser
    void testCompleteReservation_Success() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true, "Reserva completada.", testReservationResponse);
        when(reservationService.completeReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/complete", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reserva completada."));

        verify(reservationService, times(1)).completeReservation(testReservationResponse.getId());
    }

    @Test
    @WithMockUser
    void testCompleteReservation_NotFound() throws Exception {
        Long nonExistentId = 99L;
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Reserva con el id: " + nonExistentId + " no encontrada.", null);
        when(reservationService.completeReservation(nonExistentId)).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/complete", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reserva con el id: " + nonExistentId + " no encontrada."));

        verify(reservationService, times(1)).completeReservation(nonExistentId);
    }

    @Test
    @WithMockUser
    void testCompleteReservation_NotConfirmed() throws Exception {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                false, "Solo las reservas confirmadas pueden ser completadas.", null);
        when(reservationService.completeReservation(testReservationResponse.getId())).thenReturn(apiResponse);

        mockMvc.perform(put("/alquila-seg/reservations/{id}/complete", testReservationResponse.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Solo las reservas confirmadas pueden ser completadas."));

        verify(reservationService, times(1)).completeReservation(testReservationResponse.getId());
    }
}