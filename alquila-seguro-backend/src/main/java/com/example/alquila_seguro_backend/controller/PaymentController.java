package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.mercadopago.MercadoPagoService;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import com.example.alquila_seguro_backend.services.ReservationService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alquila-seg/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final MercadoPagoService mercadoPagoService;
    private final ReservationService reservationService;
    private final ConsultancyService consultancyService;

    @PostMapping("/reservations/{reservationId}/create-preference")
    public ResponseEntity<ApiResponse<String>> createPreferenceForReservation( @PathVariable Long reservationId) throws MPException, MPApiException {
        ApiResponse<ReservationResponse> reservation = reservationService.getReservationById(reservationId);
        Preference preference= mercadoPagoService.createPreferenceForReservation(reservation);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Preferencia de pago para la reserva creada correctamente.")
                .data(preference.getSandboxInitPoint())
                .build());
    }
    @PostMapping("/consultancies/{consultancyId}/create-preference")
    public ResponseEntity<ApiResponse<String>> createPreferenceForConsultancy(@PathVariable Long consultancyId) throws MPException, MPApiException {
        ApiResponse<ConsultancyResponse> consultancy = consultancyService.getConsultancyById(consultancyId);
        Preference preference= mercadoPagoService.createPreferenceForConsultancy(consultancy);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Preferencia de pago para la consultoria creada correctamente")
                .data(preference.getSandboxInitPoint())
                .build());
    }


}
