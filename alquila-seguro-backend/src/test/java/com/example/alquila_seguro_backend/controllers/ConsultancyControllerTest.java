package com.example.alquila_seguro_backend.controllers;
import com.example.alquila_seguro_backend.controller.ConsultancyController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyCreateRequest;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.security.test.context.support.WithMockUser; // Import para simular usuario autenticado
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultancyController.class)
public class ConsultancyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultancyService consultancyService;

    @MockBean // Mockea la dependencia de JwtAuthFilter
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ConsultancyResponse testConsultancyResponse;
    private ConsultancyCreateRequest testConsultancyCreateRequest;

    @BeforeEach
    void setUp() {
        // Construcción de ClientResponse sin el campo 'id', ya que no está en tu DTO
        ClientResponse sampleClientResponse = ClientResponse.builder()
                .email("cliente1@example.com")
                .firstName("Juan")
                .lastName("Pérez")
                .phone("123456789")
                .build();

        testConsultancyResponse = ConsultancyResponse.builder()
                .id(1L)
                .client(sampleClientResponse) // Usamos el ClientResponse construido
                .details("Necesito información sobre propiedades en alquiler.")
                .requestedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .totalAmount(BigDecimal.valueOf(250.00))
                .status(ConsultancyStatus.PENDING)
                .build();

        // --- MODIFICACIÓN CLAVE AQUÍ ---
        // Construcción de ConsultancyCreateRequest con los campos de cliente que proporcionaste
        testConsultancyCreateRequest = ConsultancyCreateRequest.builder()
                .clientFirstName("Juan")
                .clientLastName("Pérez")
                .clientEmail("juan.perez@example.com")
                .clientPhone("1123456789") // Número de ejemplo válido para Argentina
                .details("Nueva consulta de prueba para un inmueble.")
                .build();
        // Nota: totalAmount no está en ConsultancyCreateRequest, así que no se incluye aquí.
        // requestedAt tampoco, ya que se genera en el backend.
    }

    // --- Tests para createConsultancy ---
    @Test
    @WithMockUser
    void testCreateConsultancy_Success() throws Exception {
        // Arrange
        ApiResponse<ConsultancyResponse> apiResponse = new ApiResponse<>(
                true, "Consultoría creada exitosamente", testConsultancyResponse);
        when(consultancyService.createConsultancy(any(ConsultancyCreateRequest.class)))
                .thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(post("/alquila-seg/consultancies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConsultancyCreateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Consultoría creada exitosamente"))
                .andExpect(jsonPath("$.data.id").value(testConsultancyResponse.getId()))
                .andExpect(jsonPath("$.data.client.email").value(testConsultancyResponse.getClient().getEmail())) // Verifica el email
                .andExpect(jsonPath("$.data.client.firstName").value(testConsultancyResponse.getClient().getFirstName())) // Verifica el nombre
                .andExpect(jsonPath("$.data.status").value(testConsultancyResponse.getStatus().name()));

        verify(consultancyService, times(1)).createConsultancy(any(ConsultancyCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateConsultancy_BadRequest() throws Exception {
        // Arrange
        // Un request con campos faltantes o inválidos para simular un BadRequest
        ConsultancyCreateRequest invalidRequest = ConsultancyCreateRequest.builder()
                .clientFirstName("Corto") // No cumple Size(min=2)
                .clientLastName("") // NotBlank
                .clientEmail("invalid-email") // Not a valid email
                .clientPhone("123") // Not a valid Argentinian phone number
                .details("Detalles cortos") // Not a valid size
                .build();

        mockMvc.perform(post("/alquila-seg/consultancies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest()) // ¡CAMBIO CLAVE! Esperar 400 Bad Request
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error de validación.")) // El mensaje genérico de tu ApiResponse para MethodArgumentNotValidException
                // Verifica los errores específicos de validación en el campo 'data'
                .andExpect(jsonPath("$.data.clientLastName").value("Apellido obligatorio."))
                .andExpect(jsonPath("$.data.clientEmail").value("El email del cliente debe ser una direccion de correo valida."))
                .andExpect(jsonPath("$.data.clientPhone").value("El número de teléfono debe ser un número válido de Argentina."));
        // Agrega más assertions si tienes otras validaciones en `details` o `clientFirstName`


    }
    // --- Tests para updateConsultancyStatus ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateConsultancyStatus_Success() throws Exception {
        // Arrange
        ConsultancyStatus newStatus = ConsultancyStatus.RESPONDED;
        ConsultancyResponse updatedResponse = ConsultancyResponse.builder()
                .id(testConsultancyResponse.getId())
                .client(testConsultancyResponse.getClient())
                .details(testConsultancyResponse.getDetails())
                .requestedAt(testConsultancyResponse.getRequestedAt())
                .totalAmount(testConsultancyResponse.getTotalAmount())
                .status(newStatus)
                .build();

        ApiResponse<ConsultancyResponse> apiResponse = new ApiResponse<>(
                true, "Estado de consultoría actualizado", updatedResponse);
        when(consultancyService.updateConsultancyStatus(eq(testConsultancyResponse.getId()), eq(newStatus)))
                .thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(put("/alquila-seg/consultancies/{id}/status", testConsultancyResponse.getId())
                        .param("status", newStatus.name())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado de consultoría actualizado"))
                .andExpect(jsonPath("$.data.id").value(testConsultancyResponse.getId()))
                .andExpect(jsonPath("$.data.status").value(newStatus.name()))
                .andExpect(jsonPath("$.data.client.email").value(updatedResponse.getClient().getEmail()));

        verify(consultancyService, times(1)).updateConsultancyStatus(eq(testConsultancyResponse.getId()), eq(newStatus));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateConsultancyStatus_NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 999L;
        ConsultancyStatus newStatus = ConsultancyStatus.CLOSED;
        when(consultancyService.updateConsultancyStatus(eq(nonExistentId), eq(newStatus)))
                .thenThrow(new EntityNotFoundException("Consultoría no encontrada con ID: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/alquila-seg/consultancies/{id}/status", nonExistentId)
                        .param("status", newStatus.name())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado: Consultoría no encontrada con ID: " + nonExistentId));

        verify(consultancyService, times(1)).updateConsultancyStatus(eq(nonExistentId), eq(newStatus));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateConsultancyStatus_BadRequest_InvalidStatus() throws Exception {
        Long consultancyId = testConsultancyResponse.getId();
        mockMvc.perform(put("/alquila-seg/consultancies/{id}/status", consultancyId)
                        .param("status", "INVALID_STATUS")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("El valor 'INVALID_STATUS' no es válido para el parámetro 'status'. Se esperaba un tipo 'ConsultancyStatus'."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateConsultancyStatus_BadRequest_IllegalArgument() throws Exception {
        ConsultancyStatus validStatus = ConsultancyStatus.PENDING;
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("El estado de la consultoría no puede ser modificado a PENDING si ya está en CLOSED.");
        when(consultancyService.updateConsultancyStatus(eq(testConsultancyResponse.getId()), eq(validStatus)))
                .thenThrow(illegalArgumentException);

        mockMvc.perform(put("/alquila-seg/consultancies/{id}/status", testConsultancyResponse.getId())
                        .param("status", validStatus.name())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Datos ingresados erroneos: El estado de la consultoría no puede ser modificado a PENDING si ya está en CLOSED."));

        verify(consultancyService, times(1)).updateConsultancyStatus(eq(testConsultancyResponse.getId()), eq(validStatus));
    }
    // --- Tests para getConsultanciesByClient ---
    // Este endpoint recibe un clientId, que debería corresponder al ID de una entidad Client
    // Si tu servicio luego busca la consultoría por ese ID y devuelve ClientResponse con solo datos,
    // está bien.
    @Test
    @WithMockUser
    void testGetConsultanciesByClient_Success() throws Exception {
        Long clientId = 100L; // ID del cliente en la base de datos
        ClientResponse clientForList = ClientResponse.builder()
                .email("cliente_lista@example.com")
                .firstName("Ana")
                .lastName("Gómez")
                .phone("987654321")
                .build();

        ConsultancyResponse consultancy1 = ConsultancyResponse.builder()
                .id(2L)
                .client(clientForList) // El ClientResponse no contiene el ID del cliente
                .details("Consulta cliente 1")
                .requestedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(ConsultancyStatus.PENDING)
                .build();
        ConsultancyResponse consultancy2 = ConsultancyResponse.builder()
                .id(3L)
                .client(clientForList)
                .details("Consulta cliente 2")
                .requestedAt(LocalDateTime.now().plusHours(1))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(ConsultancyStatus.RESPONDED)
                .build();

        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "Consultorías encontradas para el cliente " + clientId, Arrays.asList(consultancy1, consultancy2));
        when(consultancyService.getConsultanciesByClient(clientId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/consultancies/client/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Consultorías encontradas para el cliente " + clientId))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].client.email").value(clientForList.getEmail()))
                .andExpect(jsonPath("$.data[1].client.firstName").value(clientForList.getFirstName())); // Verificamos un campo del ClientResponse

        verify(consultancyService, times(1)).getConsultanciesByClient(clientId);
    }

    @Test
    @WithMockUser
    void testGetConsultanciesByClient_NoContent() throws Exception {
        Long clientId = 999L;
        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "No se encontraron consultorías para el cliente " + clientId, Collections.emptyList());
        when(consultancyService.getConsultanciesByClient(clientId)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/consultancies/client/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No se encontraron consultorías para el cliente " + clientId))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(consultancyService, times(1)).getConsultanciesByClient(clientId);
    }

    // --- Tests para getConsultanciesByStatus ---
    @Test
    @WithMockUser
    void testGetConsultanciesByStatus_Success() throws Exception {
        ConsultancyStatus status = ConsultancyStatus.CLOSED;
        ClientResponse clientForStatus1 = ClientResponse.builder().email("client_status1@example.com").firstName("Pedro").build();
        ClientResponse clientForStatus2 = ClientResponse.builder().email("client_status2@example.com").firstName("Laura").build();

        ConsultancyResponse consultancy1 = ConsultancyResponse.builder()
                .id(4L)
                .client(clientForStatus1)
                .details("Detalle estado 1")
                .requestedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(300.00))
                .status(status)
                .build();
        ConsultancyResponse consultancy2 = ConsultancyResponse.builder()
                .id(5L)
                .client(clientForStatus2)
                .details("Detalle estado 2")
                .requestedAt(LocalDateTime.now().minusDays(1))
                .totalAmount(BigDecimal.valueOf(120.00))
                .status(status)
                .build();

        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "Consultorías con estado " + status.name() + " encontradas", Arrays.asList(consultancy1, consultancy2));
        when(consultancyService.getConsultanciesByStatus(status)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/consultancies/status/{status}", status.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Consultorías con estado " + status.name() + " encontradas"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value(status.name()))
                .andExpect(jsonPath("$.data[0].client.email").value(clientForStatus1.getEmail()));

        verify(consultancyService, times(1)).getConsultanciesByStatus(status);
    }

    @Test
    @WithMockUser
    void testGetConsultanciesByStatus_NoContent() throws Exception {
        ConsultancyStatus status = ConsultancyStatus.PENDING;
        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "No se encontraron consultorías con estado " + status.name(), Collections.emptyList());
        when(consultancyService.getConsultanciesByStatus(status)).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/consultancies/status/{status}", status.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No se encontraron consultorías con estado " + status.name()))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(consultancyService, times(1)).getConsultanciesByStatus(status);
    }

    // --- Tests para getConsultancyById ---
    @Test
    @WithMockUser
    void testGetConsultancyById_Success() throws Exception {
        // Arrange
        ApiResponse<ConsultancyResponse> apiResponse = new ApiResponse<>(
                true, "Consultoría encontrada", testConsultancyResponse);
        when(consultancyService.getConsultancyById(testConsultancyResponse.getId())).thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(get("/alquila-seg/consultancies/{id}", testConsultancyResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Consultoría encontrada"))
                .andExpect(jsonPath("$.data.id").value(testConsultancyResponse.getId()))
                .andExpect(jsonPath("$.data.details").value(testConsultancyResponse.getDetails()))
                .andExpect(jsonPath("$.data.client.email").value(testConsultancyResponse.getClient().getEmail()));

        verify(consultancyService, times(1)).getConsultancyById(testConsultancyResponse.getId());
    }

    @Test
    @WithMockUser
    void testGetConsultancyById_NotFound() throws Exception {
        Long nonExistentId = 999L;
        when(consultancyService.getConsultancyById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Consultoría no encontrada con ID: " + nonExistentId)); // Lanza EntityNotFoundException

        mockMvc.perform(get("/alquila-seg/consultancies/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado: Consultoría no encontrada con ID: " + nonExistentId));

        verify(consultancyService, times(1)).getConsultancyById(nonExistentId);
    }

    // --- Tests para getAllConsultancies ---
    @Test
    @WithMockUser
    void testGetAllConsultancies_Success() throws Exception {
        ClientResponse clientAll1 = ClientResponse.builder().email("client_all1@example.com").firstName("Carlos").build();
        ClientResponse clientAll2 = ClientResponse.builder().email("client_all2@example.com").firstName("Marta").build();

        ConsultancyResponse consultancy1 = ConsultancyResponse.builder()
                .id(6L)
                .client(clientAll1)
                .details("All consultoría 1")
                .requestedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(50.00))
                .status(ConsultancyStatus.PENDING)
                .build();
        ConsultancyResponse consultancy2 = ConsultancyResponse.builder()
                .id(7L)
                .client(clientAll2)
                .details("All consultoría 2")
                .requestedAt(LocalDateTime.now().minusHours(2))
                .totalAmount(BigDecimal.valueOf(75.00))
                .status(ConsultancyStatus.CLOSED)
                .build();

        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "Todas las consultorías encontradas", Arrays.asList(consultancy1, consultancy2));
        when(consultancyService.getAllConsultancies()).thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(get("/alquila-seg/consultancies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todas las consultorías encontradas"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(consultancy1.getId()))
                .andExpect(jsonPath("$.data[1].id").value(consultancy2.getId()))
                .andExpect(jsonPath("$.data[0].client.email").value(clientAll1.getEmail()));

        verify(consultancyService, times(1)).getAllConsultancies();
    }

    @Test
    @WithMockUser
    void testGetAllConsultancies_NoContent() throws Exception {
        ApiResponse<List<ConsultancyResponse>> apiResponse = new ApiResponse<>(
                true, "No hay consultorías para mostrar", Collections.emptyList());
        when(consultancyService.getAllConsultancies()).thenReturn(apiResponse);

        mockMvc.perform(get("/alquila-seg/consultancies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No hay consultorías para mostrar"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(consultancyService, times(1)).getAllConsultancies();
    }
}
