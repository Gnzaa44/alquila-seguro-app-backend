package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.CreateInvoiceRequest;
import com.example.alquila_seguro_backend.dto.InvoiceResponse;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.entity.Invoice;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;
    private Reservation reservation;
    private CreateInvoiceRequest createInvoiceRequest;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .id(100L)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                // Propiedades mínimas para evitar NPE si se acceden. No mockear completamente.
                .property(null)
                .client(null)
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .reservation(reservation)
                .totalAmount(new BigDecimal("700.00"))
                .filePath("/docs/invoice_test.pdf")
                .issuedAt(LocalDateTime.now())
                .status(DocumentStatus.PENDING)
                .build();

        createInvoiceRequest = CreateInvoiceRequest.builder()
                .reservationId(100L)
                .totalAmount(new BigDecimal("750.00"))
                .filePath("/docs/new_invoice.pdf")
                .build();
    }

    @Test
    @DisplayName("Test createInvoice should create invoice successfully")
    void testCreateInvoice_shouldCreateInvoiceSuccessfully() {
        // Given
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.createInvoice(createInvoiceRequest);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Factura generada correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getReservationId()).isEqualTo(createInvoiceRequest.getReservationId());
        assertThat(response.getData().getTotalAmount()).isEqualByComparingTo(invoice.getTotalAmount());
        assertThat(response.getData().getFilePath()).isEqualTo(invoice.getFilePath());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Test updateInvoiceStatus should update status successfully")
    void testUpdateInvoiceStatus_shouldUpdateStatusSuccessfully() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        // Crea una nueva instancia para el retorno del save para evitar modificar el objeto 'invoice' original
        Invoice updatedInvoiceEntity = Invoice.builder()
                .id(1L)
                .reservation(reservation)
                .totalAmount(invoice.getTotalAmount())
                .filePath(invoice.getFilePath())
                .issuedAt(invoice.getIssuedAt())
                .status(DocumentStatus.SENT) // Nuevo estado
                .build();
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(updatedInvoiceEntity);

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.updateInvoiceStatus(1L, DocumentStatus.SENT);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Estado de la factura actualizado correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        assertThat(response.getData().getTotalAmount()).isEqualByComparingTo(invoice.getTotalAmount());
        assertThat(response.getData().getFilePath()).isEqualTo(invoice.getFilePath());
        // Verifica que el estado en la respuesta sea el nuevo estado
        assertThat(updatedInvoiceEntity.getStatus()).isEqualTo(DocumentStatus.SENT); // Verifica el objeto que realmente se "guardó"
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Test updateInvoiceStatus should return not found when invoice does not exist")
    void testUpdateInvoiceStatus_shouldReturnNotFound_whenInvoiceDoesNotExist() {
        // Given
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.updateInvoiceStatus(99L, DocumentStatus.SENT);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Factura con el id: 99 no encontrada.");
        assertThat(response.getData()).isNull();
        verify(invoiceRepository, times(1)).findById(99L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Test getInvoiceByReservationId should return invoice by reservation ID")
    void testGetInvoiceByReservationId_shouldReturnInvoiceByReservationId() {
        // Given
        when(invoiceRepository.findByReservationId(100L)).thenReturn(Optional.of(invoice));

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceByReservationId(100L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Factura obtenida correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getReservationId()).isEqualTo(100L);
        verify(invoiceRepository, times(1)).findByReservationId(100L);
    }

    @Test
    @DisplayName("Test getInvoiceByReservationId should return not found when reservation ID does not exist")
    void testGetInvoiceByReservationId_shouldReturnNotFound_whenReservationIdDoesNotExist() {
        // Given
        when(invoiceRepository.findByReservationId(999L)).thenReturn(Optional.empty());

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceByReservationId(999L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Factura no encontrada para la reserva con el id: 999");
        assertThat(response.getData()).isNull();
        verify(invoiceRepository, times(1)).findByReservationId(999L);
    }

    @Test
    @DisplayName("Test getInvoicesByStatus should return list of invoices by status")
    void testGetInvoicesByStatus_shouldReturnListOfInvoicesByStatus() {
        // Given
        List<Invoice> invoices = Arrays.asList(invoice,
                Invoice.builder()
                        .id(2L)
                        .reservation(Reservation.builder().id(101L).build())
                        .totalAmount(new BigDecimal("500.00"))
                        .filePath("/docs/invoice_2.pdf")
                        .issuedAt(LocalDateTime.now().plusHours(1))
                        .status(DocumentStatus.PENDING)
                        .build());
        when(invoiceRepository.findByStatus(DocumentStatus.PENDING)).thenReturn(invoices);

        // When
        ApiResponse<List<InvoiceResponse>> response = invoiceService.getInvoicesByStatus(DocumentStatus.PENDING);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Facturas por estado obtenidas correctamente.");
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().getFirst().getFilePath()).isEqualTo("/docs/invoice_test.pdf");
        verify(invoiceRepository, times(1)).findByStatus(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("Test getInvoicesByStatus should return empty list when no invoices found for status")
    void testGetInvoicesByStatus_shouldReturnEmptyList_whenNoInvoicesFoundForStatus() {
        // Given
        when(invoiceRepository.findByStatus(DocumentStatus.ERROR)).thenReturn(Collections.emptyList());

        // When
        ApiResponse<List<InvoiceResponse>> response = invoiceService.getInvoicesByStatus(DocumentStatus.ERROR);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Facturas por estado obtenidas correctamente.");
        assertThat(response.getData()).isEmpty();
        verify(invoiceRepository, times(1)).findByStatus(DocumentStatus.ERROR);
    }

    @Test
    @DisplayName("Test getInvoiceById should return invoice by ID")
    void testGetInvoiceById_shouldReturnInvoiceById() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceById(1L);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Factura obtenida correctamente.");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getInvoiceById should return not found when ID does not exist")
    void testGetInvoiceById_shouldReturnNotFound_whenIdDoesNotExist() {
        // Given
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceById(99L);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Factura con el id: 99 no encontrada.");
        assertThat(response.getData()).isNull();
        verify(invoiceRepository, times(1)).findById(99L);
    }
}