package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .reservationId(invoice.getReservation().getId())
                .totalAmount(invoice.getTotalAmount())
                .issuedAt(invoice.getIssuedAt())
                .status(invoice.getStatus())
                .build();
    }
    @Transactional
    public ApiResponse<InvoiceResponse> createInvoice(Invoice invoice) {
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return ApiResponse.<InvoiceResponse>builder()
                .success(true)
                .message("Factura generada correctamente.")
                .data(mapToInvoiceResponse(savedInvoice))
                .build();
    }
    @Transactional
    public ApiResponse<InvoiceResponse> updateInvoiceStatus(Long id, DocumentStatus status) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setStatus(status);
                    Invoice updatedInvoice = invoiceRepository.save(invoice);
                    return ApiResponse.<InvoiceResponse>builder()
                            .success(true)
                            .message("Estado de la factura actualizado correctamente.")
                            .data(mapToInvoiceResponse(updatedInvoice))
                            .build();
                })
                .orElse(ApiResponse.<InvoiceResponse>builder()
                        .success(false)
                        .message("Factura con el id: " + id + " no encontrada.")
                        .build());
    }
    public ApiResponse<InvoiceResponse> getInvoiceByReservationId(Long reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .map(invoice -> ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Factura obtenida correctamente.")
                        .data(mapToInvoiceResponse(invoice))
                        .build())
                .orElse(ApiResponse.<InvoiceResponse>builder()
                        .success(false)
                        .message("Factura no encontrada para la reserva con el id: " + reservationId)
                        .build());
    }

    public ApiResponse<List<InvoiceResponse>> getInvoicesByStatus(DocumentStatus status) {
        List<InvoiceResponse> invoices = invoiceRepository.findByStatus(status).stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Facturas por estado obtenidas correctamente.")
                .data(invoices)
                .build();
    }

    public ApiResponse<InvoiceResponse> getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .map(invoice -> ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Factura obtenida correctamente.")
                        .data(mapToInvoiceResponse(invoice))
                        .build())
                .orElse(ApiResponse.<InvoiceResponse>builder()
                        .success(false)
                        .message("Factura con el id: " + id + " no encontrada.")
                        .build());
    }

}
