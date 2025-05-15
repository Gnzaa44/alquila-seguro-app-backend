package com.example.alquila_seguro_backend.mercadopago;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.PaymentStatus;
import com.example.alquila_seguro_backend.mercadopago.utils.SignatureHelper;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import com.example.alquila_seguro_backend.services.PaymentService;
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
    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;
    private final static Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${consultancy.fixed.price}")
    private BigDecimal consultancyFixedPrice;

    @Value("${mercadopago.notification-url}")
    private String notification_url;
    @Value("${mercadopago.secret-key}")
    private String secretKey;
    private final SignatureHelper signatureHelper;

    @PostConstruct
    public void logSecretKey() {
        logger.info("Valor de secretKey al inicio: {}", this.secretKey);

    }


    public Preference createPreferenceForReservation(ApiResponse<ReservationResponse> reservation) throws MPException, MPApiException {
        try {
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
                    .paymentMethods(paymentMethodsRequest)
                    .backUrls(backUrlsRequest)
                    .notificationUrl(this.notification_url)
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
        items.add(PreferenceItemRequest.builder()
                .id(String.valueOf(consultancy.getData().getId()))
                .title("Acceso a consultoria sobre una propiedad.")
                .description(consultancy.getData().getDetails())
                .categoryId("Servicio")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(this.consultancyFixedPrice)
                .build());

        PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder()
                .success("https://9791-143-202-32-28.ngrok-free.app/alquila-seg/payments/webhooks/success")
                .failure("https://9791-143-202-32-28.ngrok-free.app/alquila-seg/payments/webhooks/failure")
                .pending("https://9791-143-202-32-28.ngrok-free.app/alquila-seg/payments/webhooks/pending")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrlsRequest)
                .notificationUrl(this.notification_url)
                .externalReference(String.valueOf(consultancy.getData().getId()))
                .build();

        return client.create(preferenceRequest);
    }
    public boolean isValidWebhookNotification(HttpServletRequest request) throws IOException {
        String signatureHeader = request.getHeader("x-signature");
        String topic = request.getParameter("topic");
        String type = request.getParameter("type");

        if (signatureHeader == null || signatureHeader.isEmpty()) {
            logger.warn("Cabecera x-signature no encontrada para el topic: {}", topic);
            return false;
        }

        if ("payment".equals(type) && "payment".equals(topic)) { // Verificar ambos para consistencia (aunque 'topic' sea null)
            return signatureHelper.isValidPaymentNotificationSignature(request, signatureHeader);
        } else if ("payment".equals(type) && topic == null) { // Manejar el caso donde 'topic' es null pero 'type' es payment
            return signatureHelper.isValidPaymentNotificationSignature(request, signatureHeader);
        } else if ("merchant_order".equals(topic)) {
            logger.info("Notificación de merchant_order recibida. Omitiendo validación de firma.");
            return true;
        } else {
            logger.warn("Topic o tipo de notificación desconocido: topic={}, type={}", topic, type);
            return false;
        }
    }
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reservationId(payment.getExternalReference() != null ? Long.parseLong(payment.getExternalReference()) : null)
                .amount(payment.getTransactionAmount())
                .paymentMethod(payment.getPaymentMethodId())
                .paymentStatus(PaymentStatus.valueOf(payment.getStatus().toUpperCase())) // Convertir String a tu enum
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

    private PaymentStatus mapMercadoPagoStatusToYourStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "in_progress" -> PaymentStatus.IN_PROGRESS;
            default -> PaymentStatus.PENDING;
        };
    }

    private void updateReservationStatus(Long reservationId, String paymentStatusMP) {
        if (reservationId != null) {
            reservationService.updateReservationStatusByPayment(reservationId, paymentStatusMP);
        }
    }
    public void processPaymentUpdate(PaymentResponse paymentMP) {
        logger.info("Entrando a processPaymentUpdate con PaymentResponse: {}", paymentMP);
        Optional<com.example.alquila_seguro_backend.entity.Payment> paymentOptional = paymentRepository.findByExternalId(paymentMP.getExternalId());
        paymentOptional.ifPresent(payment -> {
            PaymentStatus newStatus = mapMercadoPagoStatusToYourStatus(paymentMP.getPaymentStatus().toString());
            payment.setPaymentStatus(newStatus);
            payment.setPaymentIdMP(paymentMP.getPaymentIdMP());
            paymentRepository.save(payment);
            logger.info("Pago actualizado en la base de datos: {}", payment);
            // ...
        });
        if (paymentOptional.isEmpty()) {
            logger.warn("No se encontró el pago con externalId: {}", paymentMP.getExternalId());
        }
    }


}
