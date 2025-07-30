package com.example.alquila_seguro_backend.mercadopago;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.mercadopago.utils.SignatureHelper;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import com.example.alquila_seguro_backend.services.EmailService;
import com.example.alquila_seguro_backend.services.ReservationService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {
    private final PreferenceClient client;
    private final PaymentClient paymentClient;
    private final static Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${consultancy.fixed.price}")
    private BigDecimal consultancyFixedPrice;

    @Value("${mercadopago.notification-url}")
    private String notification_url;
    private final SignatureHelper signatureHelper;

    public Preference createPreferenceForReservation(ApiResponse<ReservationResponse> reservation) throws MPException, MPApiException {
        try {
            long numberOfNights= ChronoUnit.DAYS.between(reservation.getData().getStartDate(), reservation.getData().getEndDate());

            String notificationUrlExternal= this.notification_url + "?source_external_reference=" + reservation.getData().getId() + "&external_entity_type=" + ExternalEntityType.RESERVATION.name();
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(PreferenceItemRequest.builder()
                    .id(String.valueOf(reservation.getData().getId()))
                    .title(reservation.getData().getProperty().getTitle())
                    .description("Reserva de " + numberOfNights + " noches desde " + reservation.getData().getStartDate() + " hasta " + reservation.getData().getEndDate())
                    .pictureUrl(Collections.singletonList(reservation.getData().getProperty().getImageUrls() != null ? reservation.getData().getProperty().getImageUrls() : "").toString())
                    .categoryId("Propiedad")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(reservation.getData().getTotalAmount())
                    .build());

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(reservation.getData().getClient().getFirstName())
                    .surname(reservation.getData().getClient().getLastName())
                    .email(reservation.getData().getClient().getEmail())
                    .build();


            List<PreferencePaymentTypeRequest> excludedPaymentTypes = null;
            if (numberOfNights < 7) {
                excludedPaymentTypes = List.of(
                        PreferencePaymentTypeRequest.builder().id("credit_card").build()
                );
            }
            PreferencePaymentMethodsRequest paymentMethodsRequest = PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentTypes(excludedPaymentTypes) // Excluir tarjeta de crédito si < 7 noches
                    .build();

            PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder()
                    .success("https://www.alquilaseguro.com.ar/success")
                    .failure("https://www.alquilaseguro.com.ar/failure")
                    .pending("https://www.alquilaseguro.com.ar/pending")
                    .build();
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .statementDescriptor("ALQUILA SEGURO")
                    .paymentMethods(paymentMethodsRequest)
                    .backUrls(backUrlsRequest)
                    .notificationUrl(notificationUrlExternal)
                    .externalReference(String.valueOf(reservation.getData().getId()))
                    .build();

            return client.create(preferenceRequest);
        } catch (MPApiException e) {
            var apiResponse = e.getApiResponse();
            var content = apiResponse.getContent();
            System.out.println(content);
        }
        throw new RuntimeException("Error inesperado al crear la preferencia de Mercado Pago.");
    }
    public Preference createPreferenceForConsultancy(ApiResponse<ConsultancyResponse> consultancy) throws MPException, MPApiException {
        List<PreferenceItemRequest> items = new ArrayList<>();

        String notificationUrlExternal= this.notification_url + "?source_external_reference=" + consultancy.getData().getId() + "&external_entity_type=" + ExternalEntityType.CONSULTANCY.name();
        items.add(PreferenceItemRequest.builder()
                .id(String.valueOf(consultancy.getData().getId()))
                .title("Acceso a consultoria sobre una propiedad.")
                .description(consultancy.getData().getDetails())
                .categoryId("Servicio")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(this.consultancyFixedPrice)
                .build());

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(consultancy.getData().getClient().getFirstName())
                .surname(consultancy.getData().getClient().getLastName())
                .email(consultancy.getData().getClient().getEmail())
                .build();


        // --- Inicia el bloque para excluir tarjeta de crédito ---
        List<PreferencePaymentTypeRequest> excludedPaymentTypes = List.of(
                PreferencePaymentTypeRequest.builder().id("credit_card").build()
        );

        PreferencePaymentMethodsRequest paymentMethodsRequest = PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excludedPaymentTypes)
                .build();
        // --- Termina el bloque para excluir tarjeta de crédito ---


        PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder()
                .success("https://www.alquilaseguro.com.ar/success")
                .failure("https://www.alquilaseguro.com.ar/failure")
                .pending("https://www.alquilaseguro.com.ar/pending")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .statementDescriptor("ALQUILA SEGURO")
                .paymentMethods(paymentMethodsRequest)
                .backUrls(backUrlsRequest)
                .notificationUrl(notificationUrlExternal)
                .externalReference(String.valueOf(consultancy.getData().getId()))
                .build();

        return client.create(preferenceRequest);
    }
    public boolean isValidWebhookNotification(HttpServletRequest request, String paymentIdMP, String dataIdFromQueryParam) throws IOException {
        String signatureHeader = request.getHeader("x-signature");
        String xRequestIdHeader = request.getHeader("x-request-id");

        // --- NUEVOS LOGS ---
        logger.info("VALIDATION DEBUG: Recibiendo notificación. URL: {}", request.getRequestURL().toString() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        logger.info("VALIDATION DEBUG: x-signature header: {}", signatureHeader);
        logger.info("VALIDATION DEBUG: x-request-id header: {}", xRequestIdHeader);
        // --- FIN NUEVOS LOGS ---


        if (signatureHeader == null || signatureHeader.isEmpty()) {
            logger.warn("Cabecera x-signature no encontrada.");
            return false;
        }
        if (dataIdFromQueryParam == null || dataIdFromQueryParam.isEmpty()) {
            logger.warn("El ID de datos (data.id) del query parameter es nulo o vacío, no se puede validar la firma para el tópico 'payment'.");
            return false;
        }
        String ts = null;
        String v1 = null;

        String[] parts = signatureHeader.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }
        // Si ts o v1 son nulos/vacíos (porque el x-signature no tenía el formato esperado)
        if (ts == null || ts.isEmpty() || v1 == null || v1.isEmpty()) {
            logger.warn("Formato inválido en x-signature. ts o v1 no encontrados. Signature: {}", signatureHeader);
            return false;
        }

        // --- NUEVOS LOGS (después del parsing de ts y v1) ---
        logger.info("VALIDATION DEBUG: ts parsed: {}", ts);
        logger.info("VALIDATION DEBUG: v1 parsed: {}", v1);
        logger.info("VALIDATION DEBUG: dataIdFromQueryParam (URL): {}", dataIdFromQueryParam);
        // --- FIN NUEVOS LOGS ---
        // Delegar la validación al SignatureHelper
        return signatureHelper.isValidPaymentNotificationSignature(dataIdFromQueryParam, xRequestIdHeader, ts, v1);
    }
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getTransactionAmount())
                .paymentMethod(payment.getPaymentMethodId())
                .paymentStatus(mapMercadoPagoStatusToYourStatus(payment.getStatus().toUpperCase())) // Convertir String a tu enum
                .paymentDate(payment.getDateCreated().toLocalDateTime())
                .paymentReference(String.valueOf(payment.getId()))
                .paymentIdMP(String.valueOf(payment.getId()))
                .paymentDescription(payment.getDescription())
                .build();
    }

    public ApiResponse<PaymentResponse> getPaymentDetails(String paymentIdMP) throws MPException, MPApiException {
        try {
            Payment payment = paymentClient.get(Long.valueOf(paymentIdMP));
            ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                    .success(true)
                    .data(mapToPaymentResponse(payment))
                    .build();
            logger.info("Respuesta de getPaymentDetails para ID (simulado) {}: {}", paymentIdMP, response); // <-- LOG AQUÍ
            return response;
        } catch (MPApiException e) {
            logger.error("MPApiException al obtener el pago {}: {}", paymentIdMP, e.getApiResponse().getContent());
            throw e;
        } catch (MPException e) {
            logger.error("MPException al obtener el pago {}: {}", paymentIdMP, e.getMessage());
            throw e;
        }
    }

    public PaymentStatus mapMercadoPagoStatusToYourStatus(String mpStatus) {
        String lowerCaseMpStatus = mpStatus.toLowerCase();
        return switch (lowerCaseMpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "in_progress" -> PaymentStatus.IN_PROGRESS;
            default -> PaymentStatus.PENDING;
        };
    }


    }

