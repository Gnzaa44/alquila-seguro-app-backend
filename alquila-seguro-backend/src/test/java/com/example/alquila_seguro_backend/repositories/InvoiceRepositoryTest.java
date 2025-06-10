package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.*; // Asegúrate de importar todas las entidades
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PropertyRepository propertyRepository;

    private Reservation testReservation;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        // Preparar entidades dependientes para evitar errores de FK
        Client client = Client.builder()
                .firstName("InvoiceClient")
                .lastName("LastName")
                .email("invoice.client@example.com")
                .phone("2235667788")
                .createdAt(LocalDateTime.now())
                .build();
        clientRepository.save(client);

        Set<String> features = new HashSet<>();
        features.add("WiFi");
        features.add("Parking");
        Property property = Property.builder()
                .pricePerNight(100.00)
                .title("Modern Apartment")
                .imageUrl("http://example.com/image.jpg")
                .description("A beautiful apartment in the city center.")
                .location("Downtown")
                .propertyStatus(PropertyStatus.AVAILABLE)
                .category("Apartment")
                .features(features)
                .build();
        propertyRepository.save(property);

        testReservation = Reservation.builder()
                .client(client)
                .property(property)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(ReservationStatus.CONFIRMED)
                .contract(null) // Si Contract es nullable en Reservation
                .build();
        reservationRepository.save(testReservation);

        testInvoice = Invoice.builder()
                .reservation(testReservation)
                .totalAmount(new BigDecimal("600.00")) // 4 days * 150/day
                .filePath("/docs/invoice_1.pdf")
                .issuedAt(LocalDateTime.now())
                .status(DocumentStatus.PENDING)
                .build();
        invoiceRepository.save(testInvoice);
    }

    @Test
    @DisplayName("Test find by ID should return invoice")
    void testFindById_shouldReturnInvoice() {
        // When
        Optional<Invoice> foundInvoice = invoiceRepository.findById(testInvoice.getId());

        // Then
        assertThat(foundInvoice).isPresent();
        assertThat(foundInvoice.get().getId()).isEqualTo(testInvoice.getId());
        assertThat(foundInvoice.get().getTotalAmount()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("Test find by Reservation ID should return invoice")
    void testFindByReservationId_shouldReturnInvoice() {
        // When
        Optional<Invoice> foundInvoice = invoiceRepository.findByReservationId(testReservation.getId());

        // Then
        assertThat(foundInvoice).isPresent();
        assertThat(foundInvoice.get().getReservation().getId()).isEqualTo(testReservation.getId());
    }

    @Test
    @DisplayName("Test find by Status should return list of invoices")
    void testFindByStatus_shouldReturnListOfInvoices() {
        // Given
        Reservation anotherReservation = reservationRepository.save(
                Reservation.builder()
                        .client(clientRepository.findById(testReservation.getClient().getId()).orElseThrow())
                        .property(propertyRepository.findById(testReservation.getProperty().getId()).orElseThrow())
                        .startDate(LocalDateTime.now().plusDays(10))
                        .endDate(LocalDateTime.now().plusDays(12))
                        .status(ReservationStatus.PENDING)
                        .contract(null)
                        .build()
        );

        Invoice anotherInvoice = Invoice.builder()
                .reservation(anotherReservation)
                .totalAmount(new BigDecimal("300.00"))
                .filePath("/docs/invoice_2.pdf")
                .issuedAt(LocalDateTime.now().plusHours(1))
                .status(DocumentStatus.PENDING) // Mismo estado
                .build();
        invoiceRepository.save(anotherInvoice);

        // When
        List<Invoice> pendingInvoices = invoiceRepository.findByStatus(DocumentStatus.PENDING);

        // Then
        assertThat(pendingInvoices).hasSize(2);
        assertThat(pendingInvoices).extracting(Invoice::getStatus).containsOnly(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("Test save invoice should persist invoice")
    void testSaveInvoice_shouldPersistInvoice() {
        // Given
        Reservation newReservation = reservationRepository.save(
                Reservation.builder()
                        .client(clientRepository.findById(testReservation.getClient().getId()).orElseThrow())
                        .property(propertyRepository.findById(testReservation.getProperty().getId()).orElseThrow())
                        .startDate(LocalDateTime.now().plusMonths(1))
                        .endDate(LocalDateTime.now().plusMonths(1).plusDays(3))
                        .status(ReservationStatus.COMPLETED)
                        .contract(null)
                        .build()
        );

        Invoice newInvoice = Invoice.builder()
                .reservation(newReservation)
                .totalAmount(new BigDecimal("450.00"))
                .filePath("/docs/invoice_3.pdf")
                .issuedAt(LocalDateTime.now().plusDays(2))
                .status(DocumentStatus.SENT)
                .build();

        // When
        Invoice savedInvoice = invoiceRepository.save(newInvoice);

        // Then
        assertThat(savedInvoice).isNotNull();
        assertThat(savedInvoice.getId()).isNotNull();
        assertThat(savedInvoice.getFilePath()).isEqualTo("/docs/invoice_3.pdf");
        assertThat(savedInvoice.getStatus()).isEqualTo(DocumentStatus.SENT);

        Optional<Invoice> found = invoiceRepository.findById(savedInvoice.getId());
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Test update invoice status should update invoice")
    void testUpdateInvoiceStatus_shouldUpdateInvoice() {
        // Given
        testInvoice.setStatus(DocumentStatus.SENT);

        // When
        Invoice updatedInvoice = invoiceRepository.save(testInvoice);

        // Then
        assertThat(updatedInvoice.getStatus()).isEqualTo(DocumentStatus.SENT);
        Optional<Invoice> found = invoiceRepository.findById(testInvoice.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(DocumentStatus.SENT);
    }
}