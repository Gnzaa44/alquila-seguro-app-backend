package com.example.alquila_seguro_backend.mercadopago;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.entity.PaymentStatus;
import com.example.alquila_seguro_backend.services.ReservationService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {
    private final PreferenceClient client;
    private final static Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${consultancy.fixed.price}")
    private BigDecimal consultancyFixedPrice;

    @Value("${mercadopago.secret-key}")
    private String secretKey;
    
    public Preference createPreferenceForReservation(ApiResponse<ReservationResponse> reservation) throws MPException, MPApiException {
        long numberOfNights= ChronoUnit.DAYS.between(reservation.getData().getStartDate(), reservation.getData().getEndDate());

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(PreferenceItemRequest.builder()
                .id(String.valueOf(reservation.getData().getId()))
                .title(reservation.getData().getProperty().getTitle())
                .description("Reserva de " + numberOfNights + " noches desde " + reservation.getData().getStartDate() + " hasta " + reservation.getData().getEndDate())
                .pictureUrl(reservation.getData().getProperty().getImageUrl() != null ? reservation.getData().getProperty().getImageUrl() : "")
                .categoryId("Propiedad")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(reservation.getData().getTotalAmount())
                .build());

        List<PreferencePaymentTypeRequest> excludedPaymentTypes = null;
        if (numberOfNights <= 7) {
            excludedPaymentTypes = List.of(
                    PreferencePaymentTypeRequest.builder().id("credit_card").build()
            );
        }
        PreferencePaymentMethodsRequest paymentMethodsRequest = PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excludedPaymentTypes) // Excluir tarjeta de crédito si <= 7 noches
                .build();

        PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder()
                .success("https://www.alquilaseguro.com.ar/success")
                .failure("https://www.alquilaseguro.com.ar/failure")
                .pending("https://www.alquilaseguro.com.ar/pending")
                .build();
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .paymentMethods(paymentMethodsRequest)
                .backUrls(backUrlsRequest)
                .build();

        return client.create(preferenceRequest);

    }
    public Preference createPreferenceForConsultancy(ApiResponse<ConsultancyResponse> consultancy) throws MPException, MPApiException {
        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(PreferenceItemRequest.builder()
                .id(String.valueOf(consultancy.getData().getId()))
                .title("Consultoría sobre " + consultancy.getData().getProperty().getTitle())
                .description(consultancy.getData().getDetails())
                .pictureUrl(consultancy.getData().getProperty().getImageUrl() != null ? consultancy.getData().getProperty().getImageUrl() : "")
                .categoryId("Servicio")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(this.consultancyFixedPrice)
                .build());

        PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder()
                .success("https://www.alquilaseguro.com.ar/success")
                .failure("https://www.alquilaseguro.com.ar/failure")
                .pending("https://www.alquilaseguro.com.ar/pending")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrlsRequest)
                .build();

        return client.create(preferenceRequest);
    }
//    public boolean isValidWebhookNotification(HttpServletRequest request) {
//        String signatureHeader = request.getHeader("x-signature");
//        if (signatureHeader == null) {
//            logger.warn("No se encontró la cabecera x-signature.");
//            return false;
//        }
//
//        String[] parts = signatureHeader.split(",");
//        String ts = null;
//        String v1 = null;
//
//        for (String part : parts) {
//            String[] keyValue = part.split("=");
//            if (keyValue.length == 2) {
//                if ("ts".equals(keyValue[0])) {
//                    ts = keyValue[1];
//                } else if ("v1".equals(keyValue[1])) { // Corrección: la firma está en v1
//                    v1 = keyValue[1];
//                }
//            }
//        }
//
//        if (ts == null || v1 == null) {
//            logger.warn("Cabecera x-signature incompleta (falta ts o v1).");
//            return false;
//        }
//
//        String dataId = request.getParameter("data.id");
//        String type = request.getParameter("type");
//        String requestId = request.getHeader("x-request-id");
//
//        if (dataId == null || type == null || requestId == null) {
//            logger.warn("Parámetros de notificación o cabecera x-request-id faltantes.");
//            return false;
//        }
//
//        String signedTemplate = String.format("id:%s;request-id:%s;ts:%s;", dataId, requestId, ts);
//
//        try {
//            String calculatedSignature = HmacUtils.hmacSha256Hex(secretKey.getBytes(StandardCharsets.UTF_8), signedTemplate.getBytes(StandardCharsets.UTF_8));
//            return calculatedSignature.equals(v1);
//        } catch (Exception e) {
//            logger.error("Error al calcular la firma HMAC: {}", e.getMessage(), e);
//            return false;
//        }
//    }

}
