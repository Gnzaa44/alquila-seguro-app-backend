package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContractRepositoryTest {

    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ReservationRepository reservationRepository; // Necesario para crear Reservation
    @Autowired
    private ClientRepository clientRepository; // Necesario para crear Client
    @Autowired
    private PropertyRepository propertyRepository; // Necesario para crear Property

    private Reservation testReservation;
    private Contract testContract;

    @BeforeEach
    void setUp() {
        // Asegúrate de que las entidades relacionadas existan para evitar errores de FK
        Client client = Client.builder()
                .firstName("TestClient")
                .lastName("TestLast")
                .email("client@example.com")
                .phone("2235667788")
                .createdAt(LocalDateTime.now())
                .build();
        clientRepository.save(client);

        Set<String> features = new HashSet<>();
        features.add("Feature 1");
        features.add("Feature 2");
        features.add("Feature 3");

        Property property = Property.builder()
                .pricePerNight(100.0)
                .title("casita")
                .imageUrl("https://lol.com/233/ss")
                .description("lololololololo")
                .location("mar del plata")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .category("casas")
                .features(features)
                .build();
        propertyRepository.save(property);

        testReservation = Reservation.builder()
                .client(client)
                .property(property)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .status(ReservationStatus.PENDING)
                .contract(null)
                .build();
        reservationRepository.save(testReservation);

        testContract = Contract.builder()
                .reservation(testReservation)
                .filePath("/docs/contract_1.pdf")
                .status(DocumentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        contractRepository.save(testContract);
    }

    @Test
    @DisplayName("Test find by ID should return contract")
    void testFindById_shouldReturnContract() {
        // When
        Optional<Contract> foundContract = contractRepository.findById(testContract.getId());

        // Then
        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getId()).isEqualTo(testContract.getId());
        assertThat(foundContract.get().getFilePath()).isEqualTo("/docs/contract_1.pdf");
    }

    @Test
    @DisplayName("Test find by Reservation ID should return contract")
    void testFindByReservationId_shouldReturnContract() {
        // When
        Optional<Contract> foundContract = contractRepository.findByReservationId(testReservation.getId());

        // Then
        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getReservation().getId()).isEqualTo(testReservation.getId());
    }

    @Test
    @DisplayName("Test find by Status should return list of contracts")
    void testFindByStatus_shouldReturnListOfContracts() {
        // Given
        Contract anotherContract = Contract.builder()
                .reservation(reservationRepository.save(
                        Reservation.builder()
                                .client(clientRepository.findById(testReservation.getClient().getId()).orElseThrow())
                                .property(propertyRepository.findById(testReservation.getProperty().getId()).orElseThrow())
                                .startDate(LocalDateTime.now().plusDays(8))
                                .endDate(LocalDateTime.now().plusDays(14))
                                .status(ReservationStatus.PENDING)
                                .build()
                ))
                .filePath("/docs/contract_2.pdf")
                .status(DocumentStatus.PENDING) // Mismo estado
                .createdAt(LocalDateTime.now().plusHours(1))
                .build();
        contractRepository.save(anotherContract);

        // When
        List<Contract> pendingContracts = contractRepository.findByStatus(DocumentStatus.PENDING);

        // Then
        assertThat(pendingContracts).hasSize(2);
        assertThat(pendingContracts).extracting(Contract::getStatus).containsOnly(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("Test save contract should persist contract")
    void testSaveContract_shouldPersistContract() {
        // Given
        Reservation newReservation = reservationRepository.save(
                Reservation.builder()
                        .client(clientRepository.findById(testReservation.getClient().getId()).orElseThrow())
                        .property(propertyRepository.findById(testReservation.getProperty().getId()).orElseThrow())
                        .startDate(LocalDateTime.now().plusMonths(1))
                        .endDate(LocalDateTime.now().plusMonths(1).plusDays(7))
                        .status(ReservationStatus.CONFIRMED)
                        .build()
        );

        Contract newContract = Contract.builder()
                .reservation(newReservation)
                .filePath("/docs/contract_3.pdf")
                .status(DocumentStatus.SENT)
                .createdAt(LocalDateTime.now().plusDays(2))
                .build();

        // When
        Contract savedContract = contractRepository.save(newContract);

        // Then
        assertThat(savedContract).isNotNull();
        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getFilePath()).isEqualTo("/docs/contract_3.pdf");
        assertThat(savedContract.getStatus()).isEqualTo(DocumentStatus.SENT);

        Optional<Contract> found = contractRepository.findById(savedContract.getId());
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Test update contract status should update contract")
    void testUpdateContractStatus_shouldUpdateContract() {
        // Given
        testContract.setStatus(DocumentStatus.SENT);

        // When
        Contract updatedContract = contractRepository.save(testContract);

        // Then
        assertThat(updatedContract.getStatus()).isEqualTo(DocumentStatus.SENT);
        Optional<Contract> found = contractRepository.findById(testContract.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(DocumentStatus.SENT);
    }

    @Test
    @DisplayName("Test delete contract should remove contract")
    void testDeleteContract_shouldRemoveContract() {
        // Given
        Long contractId = testContract.getId();

        // When
        contractRepository.deleteById(contractId);

        // Then
        Optional<Contract> found = contractRepository.findById(contractId);
        assertThat(found).isNotPresent();
    }
}