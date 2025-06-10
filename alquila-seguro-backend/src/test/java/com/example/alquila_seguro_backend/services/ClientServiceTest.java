package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientCreateRequest;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
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
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientCreateRequest clientCreateRequest;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("1122334455")
                .createdAt(LocalDateTime.now())
                .build();

        clientCreateRequest = ClientCreateRequest.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("1122334455")
                .build();

        clientResponse = ClientResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("1122334455")
                .createdAt(client.getCreatedAt()) // Asegura que el createdAt sea el mismo que el del cliente mock
                .build();
    }

    @Test
    @DisplayName("Test getAllClients should return a list of clients")
    void testGetAllClients_shouldReturnListOfClients() {
        // Given
        List<Client> clients = Arrays.asList(client,
                Client.builder().id(2L).firstName("Maria").lastName("Gomez").email("maria.gomez@example.com").phone("1198765432").createdAt(LocalDateTime.now()).build());
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        ApiResponse<List<ClientResponse>> response = clientService.getAllClients();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Clientes recuperados correctamente.");
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getEmail()).isEqualTo("juan.perez@example.com");
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getAllClients should return empty list when no clients exist")
    void testGetAllClients_shouldReturnEmptyList_whenNoClientsExist() {
        // Given
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ApiResponse<List<ClientResponse>> response = clientService.getAllClients();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Clientes recuperados correctamente.");
        assertThat(response.getData()).isEmpty();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getClientById should return client when id exists")
    void testGetClientById_shouldReturnClient_whenIdExists() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        // When
        ApiResponse<ClientResponse> response = clientService.getClientById(1L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Cliente recuperado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        assertThat(response.getData().getEmail()).isEqualTo("juan.perez@example.com");
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getClientById should return not found when id does not exist")
    void testGetClientById_shouldReturnNotFound_whenIdDoesNotExist() {
        // Given
        when(clientRepository.findById(2L)).thenReturn(Optional.empty());

        // When
        ApiResponse<ClientResponse> response = clientService.getClientById(2L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Cliente con id : 2 no encontrado");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Test createClient should create client successfully")
    void testCreateClient_shouldCreateClientSuccessfully() {
        // Given
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client); // Usa el 'client' mockeado para asegurar que tenga ID y createdAt

        // When
        ApiResponse<ClientResponse> response = clientService.createClient(clientCreateRequest);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Cliente registrado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo(clientCreateRequest.getEmail());
        verify(clientRepository, times(1)).existsByEmail(clientCreateRequest.getEmail());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Test createClient should return error if email already exists")
    void testCreateClient_shouldReturnError_ifEmailAlreadyExists() {
        // Given
        when(clientRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        ApiResponse<ClientResponse> response = clientService.createClient(clientCreateRequest);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Mail ya en uso");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).existsByEmail(clientCreateRequest.getEmail());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test updateClient should update client successfully")
    void testUpdateClient_shouldUpdateClientSuccessfully() {
        // Given
        Client existingClient = Client.builder()
                .id(1L)
                .firstName("OldFirstName")
                .lastName("OldLastName")
                .email("old.email@example.com")
                .phone("1111111111")
                .createdAt(LocalDateTime.now())
                .build();

        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .firstName("NewFirstName")
                .lastName("NewLastName")
                .email("new.email@example.com")
                .phone("2222222222")
                .build();

        Client updatedClientEntity = Client.builder()
                .id(1L)
                .firstName("NewFirstName")
                .lastName("NewLastName")
                .email("new.email@example.com")
                .phone("2222222222")
                .createdAt(existingClient.getCreatedAt())
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByEmail("new.email@example.com")).thenReturn(false); // New email does not exist
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClientEntity);

        // When
        ApiResponse<ClientResponse> response = clientService.updateClient(1L, updateRequest);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Cliente actualizado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo("new.email@example.com");
        assertThat(response.getData().getFirstName()).isEqualTo("NewFirstName");
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).existsByEmail("new.email@example.com");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Test updateClient should not update client if new email already exists")
    void testUpdateClient_shouldNotUpdateClient_ifNewEmailAlreadyExists() {
        // Given
        Client existingClient = Client.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("2234567890")
                .createdAt(LocalDateTime.now())
                .build();

        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("existing@example.com") // Intenta cambiar a un email existente
                .phone("2235664312")
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByEmail("existing@example.com")).thenReturn(true); // El nuevo email ya existe

        // When
        ApiResponse<ClientResponse> response = clientService.updateClient(1L, updateRequest);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("El mail ya existe.");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).existsByEmail("existing@example.com");
        verify(clientRepository, never()).save(any(Client.class)); // No debería intentar guardar
    }

    @Test
    @DisplayName("Test updateClient should update client when email is not changed")
    void testUpdateClient_shouldUpdateClient_whenEmailIsNotChanged() {
        // Given
        Client existingClient = Client.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("2235778645")
                .createdAt(LocalDateTime.now())
                .build();

        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .firstName("JuanUpdated")
                .lastName("PerezUpdated")
                .email("juan.perez@example.com") // Email no cambia
                .phone("2235667788")
                .build();

        Client updatedClientEntity = Client.builder()
                .id(1L)
                .firstName("JuanUpdated")
                .lastName("PerezUpdated")
                .email("juan.perez@example.com")
                .phone("2235431233")
                .createdAt(existingClient.getCreatedAt())
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        // No se llama a existsByEmail si el email no cambia
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClientEntity);

        // When
        ApiResponse<ClientResponse> response = clientService.updateClient(1L, updateRequest);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Cliente actualizado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(response.getData().getFirstName()).isEqualTo("JuanUpdated");
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, never()).existsByEmail(anyString()); // No se llama a existsByEmail
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Test updateClient should return not found when id does not exist")
    void testUpdateClient_shouldReturnNotFound_whenIdDoesNotExist() {
        // Given
        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .firstName("NewFirstName")
                .lastName("NewLastName")
                .email("new.email@example.com")
                .phone("2235667634")
                .build();
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<ClientResponse> response = clientService.updateClient(99L, updateRequest);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Cliente con el id: 99 no encontrado.");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).findById(99L);
        verify(clientRepository, never()).existsByEmail(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test deleteClient should delete client successfully")
    void testDeleteClient_shouldDeleteClientSuccessfully() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).delete(any(Client.class));

        // When
        ApiResponse<Void> response = clientService.deleteClient(1L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Cliente eliminado correctamente.");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(any(Client.class));
    }

    @Test
    @DisplayName("Test deleteClient should return not found when id does not exist")
    void testDeleteClient_shouldReturnNotFound_whenIdDoesNotExist() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<Void> response = clientService.deleteClient(99L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Cliente con id: 99 no encontrado.");
        assertThat(response.getData()).isNull();
        verify(clientRepository, times(1)).findById(99L);
        verify(clientRepository, never()).delete(any(Client.class));
    }
}