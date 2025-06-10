package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("Test findByEmail should return Client when email exists")
    void testFindByEmail_shouldReturnClient_whenEmailExists() {
        // Given
        Client client = Client.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan.perez@example.com")
                .phone("2234567213")
                .createdAt(LocalDateTime.now())
                .build();
        clientRepository.save(client);

        // When
        Optional<Client> foundClient = clientRepository.findByEmail("juan.perez@example.com");

        // Then
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getEmail()).isEqualTo("juan.perez@example.com");
    }

    @Test
    @DisplayName("Test findByEmail should return empty when email does not exist")
    void testFindByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        // When
        Optional<Client> foundClient = clientRepository.findByEmail("non.existent@example.com");

        // Then
        assertThat(foundClient).isEmpty();
    }

    @Test
    @DisplayName("Test existsByEmail should return true when email exists")
    void testExistsByEmail_shouldReturnTrue_whenEmailExists() {
        // Given
        Client client = Client.builder()
                .firstName("Maria")
                .lastName("Gomez")
                .email("maria.gomez@example.com")
                .phone("2235663290")
                .createdAt(LocalDateTime.now())
                .build();
        clientRepository.save(client);

        // When
        boolean exists = clientRepository.existsByEmail("maria.gomez@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Test existsByEmail should return false when email does not exist")
    void testExistsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        // When
        boolean exists = clientRepository.existsByEmail("non.existent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Test save client")
    void testSaveClient() {
        // Given
        Client client = Client.builder()
                .firstName("Carlos")
                .lastName("Lopez")
                .email("carlos.lopez@example.com")
                .phone("2235987623")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        Client savedClient = clientRepository.save(client);

        // Then
        assertThat(savedClient).isNotNull();
        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getFirstName()).isEqualTo("Carlos");
        assertThat(clientRepository.findById(savedClient.getId())).isPresent();
    }

    @Test
    @DisplayName("Test find all clients")
    void testFindAllClients() {
        // Given
        Client client1 = Client.builder().firstName("Client1").lastName("Last1").email("c1@example.com").phone("2234567890").createdAt(LocalDateTime.now()).build();
        Client client2 = Client.builder().firstName("Client2").lastName("Last2").email("c2@example.com").phone("2234554312").createdAt(LocalDateTime.now()).build();
        clientRepository.saveAll(Arrays.asList(client1, client2));

        // When
        List<Client> clients = clientRepository.findAll();

        // Then
        assertThat(clients).hasSize(2);
    }

    @Test
    @DisplayName("Test delete client by id")
    void testDeleteClientById() {
        // Given
        Client client = Client.builder().firstName("ClientToDelete").lastName("Delete").email("delete@example.com").phone("2234667854").createdAt(LocalDateTime.now()).build();
        Client savedClient = clientRepository.save(client);

        // When
        clientRepository.deleteById(savedClient.getId());
        Optional<Client> deletedClient = clientRepository.findById(savedClient.getId());

        // Then
        assertThat(deletedClient).isNotPresent();
    }
}