package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyCreateRequest;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
public class ConsultancyServiceTest {
    @Mock
    private ConsultancyRepository consultancyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConsultancyService consultancyService;

    private Client existingClient;
    private ConsultancyCreateRequest createRequest;
    private Consultancy pendingConsultancy;
    private Consultancy respondedConsultancy; // Cambiado de 'approved' a 'responded'
    private Consultancy closedConsultancy;    // Añadido para el estado 'closed'

    private final String veedorEmail = "veedor@example.com";

    @BeforeEach
    void setUp() {
        // Inyección del valor @Value
        ReflectionTestUtils.setField(consultancyService, "emailVeedor", veedorEmail);

        existingClient = Client.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("123456789")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = ConsultancyCreateRequest.builder()
                .clientFirstName("Juan")
                .clientLastName("Perez")
                .clientEmail("juan.perez@example.com")
                .clientPhone("123456789")
                .details("Consulta sobre alquiler de propiedad X")
                .build();

        pendingConsultancy = Consultancy.builder()
                .id(10L)
                .client(existingClient)
                .details("Consulta sobre alquiler de propiedad X")
                .requestedAt(LocalDateTime.now().minusHours(1))
                .status(ConsultancyStatus.PENDING)
                .build();

        respondedConsultancy = Consultancy.builder() // Renombrado
                .id(11L)
                .client(existingClient)
                .details("Consulta sobre alquiler de propiedad Y")
                .requestedAt(LocalDateTime.now().minusHours(2))
                .status(ConsultancyStatus.RESPONDED) // Estado ajustado
                .build();

        closedConsultancy = Consultancy.builder() // Nuevo objeto para estado CLOSED
                .id(12L)
                .client(existingClient)
                .details("Consulta sobre alquiler de propiedad Z")
                .requestedAt(LocalDateTime.now().minusHours(3))
                .status(ConsultancyStatus.CLOSED)
                .build();
    }

    // --- Tests para createConsultancy ---
    @Test
    void testCreateConsultancy_ExistingClient() throws MessagingException {
        when(clientRepository.findByEmail(createRequest.getClientEmail()))
                .thenReturn(Optional.of(existingClient));
        when(consultancyRepository.save(any(Consultancy.class)))
                .thenReturn(pendingConsultancy);

        ApiResponse<ConsultancyResponse> response = consultancyService.createConsultancy(createRequest);

        assertTrue(response.isSuccess());
        assertEquals("Consultoria creada correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(pendingConsultancy.getId(), response.getData().getId());
        assertEquals(existingClient.getId(), response.getData().getClient().getId());

        verify(clientRepository, times(1)).findByEmail(createRequest.getClientEmail());
        verify(clientRepository, never()).save(any(Client.class));
        verify(consultancyRepository, times(1)).save(any(Consultancy.class));

        verify(emailService, times(1)).sendEmail(
                eq(veedorEmail),
                eq("Nueva Consultoría Solicitada"),
                anyString()
        );
    }

    @Test
    void testCreateConsultancy_NewClient() throws MessagingException {
        when(clientRepository.findByEmail(createRequest.getClientEmail()))
                .thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class)))
                .thenReturn(existingClient);
        when(consultancyRepository.save(any(Consultancy.class)))
                .thenReturn(pendingConsultancy);

        ApiResponse<ConsultancyResponse> response = consultancyService.createConsultancy(createRequest);

        assertTrue(response.isSuccess());
        assertEquals("Consultoria creada correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(pendingConsultancy.getId(), response.getData().getId());
        assertEquals(existingClient.getId(), response.getData().getClient().getId());

        verify(clientRepository, times(1)).findByEmail(createRequest.getClientEmail());
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(consultancyRepository, times(1)).save(any(Consultancy.class));

        verify(emailService, times(1)).sendEmail(
                eq(veedorEmail),
                eq("Nueva Consultoría Solicitada"),
                anyString()
        );
    }

    @Test
    void testCreateConsultancy_EmailSendFails() throws MessagingException {
        when(clientRepository.findByEmail(createRequest.getClientEmail()))
                .thenReturn(Optional.of(existingClient));
        when(consultancyRepository.save(any(Consultancy.class)))
                .thenReturn(pendingConsultancy);
        doThrow(new RuntimeException("Error de envío de mail")).when(emailService).sendEmail(
                anyString(), anyString(), anyString());

        ApiResponse<ConsultancyResponse> response = consultancyService.createConsultancy(createRequest);

        assertTrue(response.isSuccess());
        assertEquals("Consultoria creada correctamente.", response.getMessage());
        assertNotNull(response.getData());

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    // --- Tests para updateConsultancyStatus ---
    @Test
    void testUpdateConsultancyStatus_Success() {
        // Testeando el cambio a RESPONDED
        when(consultancyRepository.findById(pendingConsultancy.getId()))
                .thenReturn(Optional.of(pendingConsultancy));
        // Mockea el resultado de save para que refleje el nuevo estado
        Consultancy updatedToResponded = Consultancy.builder()
                .id(pendingConsultancy.getId())
                .client(pendingConsultancy.getClient())
                .details(pendingConsultancy.getDetails())
                .requestedAt(pendingConsultancy.getRequestedAt())
                .status(ConsultancyStatus.RESPONDED) // Nuevo estado
                .build();
        when(consultancyRepository.save(any(Consultancy.class)))
                .thenReturn(updatedToResponded);

        ApiResponse<ConsultancyResponse> response = consultancyService.updateConsultancyStatus(
                pendingConsultancy.getId(), ConsultancyStatus.RESPONDED);

        assertTrue(response.isSuccess());
        assertEquals("Estado de la consultoria actualizado correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(ConsultancyStatus.RESPONDED, response.getData().getStatus());

        ArgumentCaptor<Consultancy> consultancyCaptor = ArgumentCaptor.forClass(Consultancy.class);
        verify(consultancyRepository, times(1)).save(consultancyCaptor.capture());
        assertEquals(ConsultancyStatus.RESPONDED, consultancyCaptor.getValue().getStatus());

        verify(consultancyRepository, times(1)).findById(pendingConsultancy.getId());
    }

    @Test
    void testUpdateConsultancyStatus_NotFound() {
        when(consultancyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ApiResponse<ConsultancyResponse> response = consultancyService.updateConsultancyStatus(
                99L, ConsultancyStatus.CLOSED); // Usando un estado válido

        assertFalse(response.isSuccess());
        assertEquals("Consultancy not found", response.getMessage());
        assertNull(response.getData());

        verify(consultancyRepository, times(1)).findById(99L);
        verify(consultancyRepository, never()).save(any(Consultancy.class));
    }

    // --- Tests para getConsultanciesByClient ---
    @Test
    void testGetConsultanciesByClient_Found() {
        when(consultancyRepository.findByClientId(existingClient.getId()))
                .thenReturn(Arrays.asList(pendingConsultancy, respondedConsultancy)); // Estados ajustados

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getConsultanciesByClient(existingClient.getId());

        assertTrue(response.isSuccess());
        assertEquals("Consultorias por cliente obtenidas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals(pendingConsultancy.getId(), response.getData().get(0).getId());
        assertEquals(respondedConsultancy.getId(), response.getData().get(1).getId());

        verify(consultancyRepository, times(1)).findByClientId(existingClient.getId());
    }

    @Test
    void testGetConsultanciesByClient_NotFound() {
        when(consultancyRepository.findByClientId(anyLong()))
                .thenReturn(Collections.emptyList());

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getConsultanciesByClient(99L);

        assertTrue(response.isSuccess());
        assertEquals("Consultorias por cliente obtenidas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(consultancyRepository, times(1)).findByClientId(99L);
    }

    // --- Tests para getConsultanciesByStatus ---
    @Test
    void testGetConsultanciesByStatus_PendingFound() {
        when(consultancyRepository.findByStatus(ConsultancyStatus.PENDING))
                .thenReturn(Collections.singletonList(pendingConsultancy));

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getConsultanciesByStatus(ConsultancyStatus.PENDING);

        assertTrue(response.isSuccess());
        assertEquals("Consultorias por estado obtenidas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(pendingConsultancy.getId(), response.getData().get(0).getId());

        verify(consultancyRepository, times(1)).findByStatus(ConsultancyStatus.PENDING);
    }

    @Test
    void testGetConsultanciesByStatus_RespondedFound() {
        when(consultancyRepository.findByStatus(ConsultancyStatus.RESPONDED))
                .thenReturn(Collections.singletonList(respondedConsultancy));

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getConsultanciesByStatus(ConsultancyStatus.RESPONDED);

        assertTrue(response.isSuccess());
        assertEquals("Consultorias por estado obtenidas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(respondedConsultancy.getId(), response.getData().get(0).getId());

        verify(consultancyRepository, times(1)).findByStatus(ConsultancyStatus.RESPONDED);
    }

    @Test
    void testGetConsultanciesByStatus_NotFound() {
        when(consultancyRepository.findByStatus(any(ConsultancyStatus.class)))
                .thenReturn(Collections.emptyList());

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getConsultanciesByStatus(ConsultancyStatus.CLOSED); // Usando un estado válido

        assertTrue(response.isSuccess());
        assertEquals("Consultorias por estado obtenidas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(consultancyRepository, times(1)).findByStatus(ConsultancyStatus.CLOSED);
    }

    // --- Tests para getConsultancyById ---
    @Test
    void testGetConsultancyById_Found() {
        when(consultancyRepository.findById(pendingConsultancy.getId()))
                .thenReturn(Optional.of(pendingConsultancy));

        ApiResponse<ConsultancyResponse> response = consultancyService.getConsultancyById(pendingConsultancy.getId());

        assertTrue(response.isSuccess());
        assertEquals("Consultoria obtenida correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(pendingConsultancy.getId(), response.getData().getId());

        verify(consultancyRepository, times(1)).findById(pendingConsultancy.getId());
    }

    @Test
    void testGetConsultancyById_NotFound() {
        when(consultancyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ApiResponse<ConsultancyResponse> response = consultancyService.getConsultancyById(99L);

        assertFalse(response.isSuccess());
        assertEquals("Consultoria no encontrada.", response.getMessage());
        assertNull(response.getData());

        verify(consultancyRepository, times(1)).findById(99L);
    }

    // --- Tests para getAllConsultancies ---
    @Test
    void testGetAllConsultancies_Found() {
        when(consultancyRepository.findAll())
                .thenReturn(Arrays.asList(pendingConsultancy, respondedConsultancy, closedConsultancy)); // Todos los estados

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getAllConsultancies();

        assertTrue(response.isSuccess());
        assertEquals("Consultorias recuperadas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size()); // Se esperan 3 consultorías
        assertEquals(pendingConsultancy.getId(), response.getData().get(0).getId());
        assertEquals(respondedConsultancy.getId(), response.getData().get(1).getId());
        assertEquals(closedConsultancy.getId(), response.getData().get(2).getId());

        verify(consultancyRepository, times(1)).findAll();
    }

    @Test
    void testGetAllConsultancies_NotFound() {
        when(consultancyRepository.findAll())
                .thenReturn(Collections.emptyList());

        ApiResponse<List<ConsultancyResponse>> response = consultancyService.getAllConsultancies();

        assertTrue(response.isSuccess());
        assertEquals("Consultorias recuperadas correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(consultancyRepository, times(1)).findAll();
    }
}
