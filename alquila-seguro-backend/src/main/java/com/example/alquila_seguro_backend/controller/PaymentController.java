package com.example.alquila_seguro_backend.controller;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.mercadopago.MercadoPagoService;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import com.example.alquila_seguro_backend.services.ConsultancyService;
import com.example.alquila_seguro_backend.services.EmailService;
import com.example.alquila_seguro_backend.services.PaymentService;
import com.example.alquila_seguro_backend.services.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController

@RequestMapping("/alquila-seg/payments")

@RequiredArgsConstructor

public class PaymentController {

    private final MercadoPagoService mercadoPagoService;
    private final ReservationService reservationService;
    private final ConsultancyService consultancyService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Value("${consultancy.fixed.price}")
    private BigDecimal consultancyTotal;
    private final EmailService emailService;


    @PostMapping("/reservations/{reservationId}/create-preference")
    public ResponseEntity<ApiResponse<String>> createPreferenceForReservation( @PathVariable Long reservationId) throws MPException, MPApiException {
        ApiResponse<ReservationResponse> reservation = reservationService.getReservationById(reservationId);
        if (!reservation.isSuccess() || reservation.getData() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder().success(false).message(reservation.getMessage()).build());
        }
        Preference preference= mercadoPagoService.createPreferenceForReservation(reservation);
         paymentService.createPayment(
                reservation.getData().getId(),
                null, // consultancyId es null
                reservation.getData().getTotalAmount(),
                "Mercado Pago",
                preference.getId(),
                "Creación de preferencia de reserva"
        );
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Preferencia de pago para la reserva creada correctamente.")
                .data(preference.getId())
                .build());
    }
    @PostMapping("/consultancies/{consultancyId}/create-preference")
    public ResponseEntity<ApiResponse<String>> createPreferenceForConsultancy(@PathVariable Long consultancyId) throws MPException, MPApiException {
        ApiResponse<ConsultancyResponse> consultancy = consultancyService.getConsultancyById(consultancyId);
        if (!consultancy.isSuccess() || consultancy.getData() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder().success(false).message(consultancy.getMessage()).build());
        }
        Preference preference= mercadoPagoService.createPreferenceForConsultancy(consultancy);
        paymentService.createPayment(
                null, // reservationId es null
                consultancy.getData().getId(),
                this.consultancyTotal,
                "Mercado Pago",
                preference.getId(),
                "Creación de preferencia de consultoría"
        );
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Preferencia de pago para la consultoria creada correctamente")
                .data(preference.getId())
                .build());
    }
    @PostMapping("/webhooks")
    public ResponseEntity<String> receivePaymentNotification(
            HttpServletRequest request) throws IOException, InterruptedException, MPException, MPApiException {
        String queryTopic = request.getParameter("topic"); // topic del query param
        String queryType = request.getParameter("type");   // type del query param (raro, pero posible)
        String queryDataId = request.getParameter("data.id"); // data.id del query param (para payment.created)
        String queryId = request.getParameter("id");       // id del query param (para merchant_order o topic=payment)

        String externalReferenceFromUrlParam = request.getParameter("source_external_reference");
        String externalEntityTypeFromUrlParam = request.getParameter("external_entity_type");


        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            logger.error("Error al leer el cuerpo de la solicitud: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al procesar la solicitud");
        }
        logger.info("Cuerpo completo de la notificación: {}", requestBody.toString());

        String jsonTopic = null;
        String jsonType = null;
        String jsonAction = null;
        String jsonResourceId = null;
        String jsonPaymentIdMP = null; // ID de pago extraído del JSON

        try {
            if (requestBody.length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(requestBody.toString());

                jsonAction = root.path("action").asText(null);
                jsonType = root.path("type").asText(null);
                jsonTopic = root.path("topic").asText(null); // A veces topic viene en el body
                JsonNode dataNode = root.path("data");

                if (dataNode.has("id")) {
                    jsonPaymentIdMP = dataNode.path("id").asText(null);
                } else if (root.has("id")) { // Para casos donde el ID es la raíz (ej. algunas notificaciones legacy)
                    jsonPaymentIdMP = root.path("id").asText(null);
                }

                if (root.has("resource")) {
                    jsonResourceId = root.path("resource").asText(null);
                }

                if (root.has("external_reference")) {
                    logger.info("External Reference encontrado en cuerpo (inesperado para payment.updated/created): {}", root.path("external_reference").asText(null));
                }
            } else {
                logger.warn("Cuerpo de la notificación vacío o nulo.");
            }
        } catch (Exception e) {
            logger.warn("Error al parsear el cuerpo de la notificación: {}", e.getMessage(), e);
        }

        // --- Lógica para determinar los valores finales (priorizando body sobre query params) ---
        String finalTopic = (jsonTopic != null && !jsonTopic.isEmpty()) ? jsonTopic : queryTopic;
        String finalType = (jsonType != null && !jsonType.isEmpty()) ? jsonType : queryType;
        String finalAction = (jsonAction != null && !jsonAction.isEmpty()) ? jsonAction : null; // action solo del body

        // El ID de pago final a utilizar para buscar en tu DB y MP
        String finalPaymentIdMP = null;
        if (jsonPaymentIdMP != null) { // Prioriza ID del JSON (data.id o id directo)
            finalPaymentIdMP = jsonPaymentIdMP;
        } else if (jsonResourceId != null && jsonResourceId.contains("/payments/")) { // Si es una URL de recurso
            finalPaymentIdMP = jsonResourceId.substring(jsonResourceId.lastIndexOf("/") + 1);
        } else if (jsonResourceId != null) { // Si es solo un ID en 'resource'
            finalPaymentIdMP = jsonResourceId;
        } else if (queryDataId != null) { // Para notifications con data.id en query params
            finalPaymentIdMP = queryDataId;
        } else if (queryId != null && "payment".equals(queryTopic)) { // Para notifications con id en query params y topic=payment
            finalPaymentIdMP = queryId;
        } else if (queryId != null && "merchant_order".equals(queryTopic)) { // para merchant_order id
            finalPaymentIdMP = queryId;
        }

        logger.info("Notificación recibida - Topic: {}, Type: {}, Action: {}, Payment ID (final): {}, Resource ID (JSON): {}, External Reference (URL param): {}, External Entity Type from Param: {}",
                finalTopic, finalType, finalAction, finalPaymentIdMP, jsonResourceId, externalReferenceFromUrlParam, externalEntityTypeFromUrlParam);

        // --- RAMAS DE PROCESAMIENTO ---

        // 1. Notificaciones de ORDEN COMERCIAL (topic=merchant_order)
        if ("merchant_order".equals(finalTopic)) {
            logger.info("DEBUG: Entrando a la rama de 'merchant_order'.");
            logger.info("Notificación de orden comercial recibida. Respondiendo con OK.");
            return ResponseEntity.ok("Notificación de orden comercial recibida");
        }
        // 2. Notificaciones de PAGO (topic=payment O type=payment)
        // Incluye payment.created y actualizaciones de estado
        else if (("payment".equals(finalTopic) || "payment".equals(finalType)) && finalPaymentIdMP != null) {
            logger.info("DEBUG: Entrando a la rama de 'payment' (creado o actualizado).");
            logger.info("Recibida notificación de pago con ID: {}", finalPaymentIdMP);

            if (externalReferenceFromUrlParam == null || externalEntityTypeFromUrlParam == null) {
                logger.warn("Notificación de pago sin source_external_reference o external_entity_type. No se puede procesar correctamente.");
                // Aunque no podamos procesar la referencia, respondemos OK para no causar reintentos excesivos
                return ResponseEntity.badRequest().body("Notificación sin los parámetros de referencia necesarios.");
            }

            boolean isSignatureValid = false;
            try {
                // Aquí, 'queryDataId' se usa como el 'id' de la URL para la validación de firma,
                // mientras que 'finalPaymentIdMP' es el ID de pago de Mercado Pago que usaremos para buscar.
                isSignatureValid = mercadoPagoService.isValidWebhookNotification(request, queryDataId, finalPaymentIdMP);
            } catch (IOException e) {
                logger.error("Error al leer cuerpo de la solicitud para validar firma: {}", e.getMessage(), e);
                // No retornamos inmediatamente, intentamos el fallback
            }


            if (isSignatureValid) {
                logger.info("DEBUG: Firma de notificación de pago VÁLIDA. Procesando detalles.");
                try {
                    processPaymentNotification(finalPaymentIdMP, externalReferenceFromUrlParam, ExternalEntityType.valueOf(externalEntityTypeFromUrlParam));
                } catch (MPException | MPApiException e) {
                    logger.error("Error al procesar la notificación de pago (firma válida): {}", e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la notificación de pago.");
                }
            } else {
                logger.warn("DEBUG: Firma de notificación de pago INVÁLIDA para ID: {}. Intentando fallback via API.", finalPaymentIdMP);
                // --- INICIO DEL FALLBACK POR API ---
                try {
                    logger.info("DEBUG: Intentando obtener detalles del pago {} directamente de la API de Mercado Pago.", finalPaymentIdMP);
                    // Usar finalPaymentIdMP para la llamada a la API
                    ApiResponse<PaymentResponse> apiResponse = mercadoPagoService.getPaymentDetails(finalPaymentIdMP);

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        PaymentResponse paymentDetails = apiResponse.getData();
                        logger.info("DEBUG: Detalles del pago {} obtenidos exitosamente de la API. Estado: {}", finalPaymentIdMP, paymentDetails.getPaymentStatus());
                        // Procesa la notificación usando los datos obtenidos de la API
                        paymentService.updatePaymentStatusFromMercadoPago(
                                paymentDetails.getId().toString(),
                                paymentDetails.getPaymentStatus().toString(),
                                paymentDetails.getPaymentMethod(),
                                paymentDetails.getPaymentReference(),
                                externalReferenceFromUrlParam, // Parámetros de referencia del webhook
                                ExternalEntityType.valueOf(externalEntityTypeFromUrlParam)
                        );
                        logger.info("DEBUG: Notificación de pago procesada con éxito via FALLBACK API para ID: {}", finalPaymentIdMP);
                    } else {
                        logger.error("ERROR: Fallback API para pago {} falló. Mensaje: {}. No se pudo verificar el estado.", finalPaymentIdMP, apiResponse.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to verify payment via API fallback.");
                    }
                } catch (Exception e) { // Captura cualquier excepción durante el fallback
                    logger.error("ERROR: Excepción durante el fallback API para pago {}: {}", finalPaymentIdMP, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during API fallback for payment verification.");
                }
                // --- FIN DEL FALLBACK POR API ---
            }
            logger.info("Datos notificación procesados para Payment ID: {}", finalPaymentIdMP);
            return ResponseEntity.ok("Notificación de pago procesada.");
        }
        // 3. Notificaciones no manejadas
        else {
            logger.warn("DEBUG: Entrando a la rama 'Notificación no manejada'.");
            logger.warn("Notificación no manejada: Topic={}, Type={}, Action={}, Payment ID={}, Raw Body: {}", finalTopic, finalType, finalAction, finalPaymentIdMP, requestBody.toString());
            return ResponseEntity.ok("Notificación no manejada, pero recibida correctamente.");
        }


    }
    private void processPaymentNotification(String paymentIdMP, String externalIdFromUrl, ExternalEntityType externalEntityTypeFromUrl) throws MPException, MPApiException {
        logger.info("processPaymentNotification iniciado para el ID: {}, externalId: {}, externalEntityType: {}", paymentIdMP, externalIdFromUrl, externalEntityTypeFromUrl);

        // Primero, intenta obtener los detalles completos del pago desde Mercado Pago
        // Esto es CRUCIAL para obtener el status final, amount, etc.
        ApiResponse<PaymentResponse> mpPaymentDetailsResponse = mercadoPagoService.getPaymentDetails(paymentIdMP);

        if (mpPaymentDetailsResponse.isSuccess() && mpPaymentDetailsResponse.getData() != null) {
            PaymentResponse paymentMPDetails = mpPaymentDetailsResponse.getData();
            logger.info("Detalles del pago obtenidos exitosamente para ID {}: {}", paymentIdMP, paymentMPDetails);

            // *** CAMBIO CLAVE: Usa externalIdFromUrl y externalEntityTypeFromUrl para encontrar el pago en tu DB ***
            // Esta es la búsqueda que debe ser única y no causar el error.
            Optional<Payment> paymentOptional =
                    paymentRepository.findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(
                            externalIdFromUrl, externalEntityTypeFromUrl, PaymentStatus.PENDING); // Busca el PENDING para actualizar

            com.example.alquila_seguro_backend.entity.Payment paymentToUpdate = null;

            if (paymentOptional.isPresent()) {
                paymentToUpdate = paymentOptional.get();
                logger.info("Pago PENDING encontrado en tu DB para externalId: {} y externalEntityType: {}. ID del pago en tu DB: {}",
                        externalIdFromUrl, externalEntityTypeFromUrl, paymentToUpdate.getId());

                PaymentStatus newStatus = mercadoPagoService.mapMercadoPagoStatusToYourStatus(String.valueOf(paymentMPDetails.getPaymentStatus()));

                // Solo actualiza si hay un cambio de estado o si el paymentIdMP aún no está seteado
                if (!paymentToUpdate.getPaymentStatus().equals(newStatus) || paymentToUpdate.getPaymentIdMP() == null || !paymentToUpdate.getPaymentIdMP().equals(paymentIdMP)) {
                    if (paymentToUpdate.getPaymentIdMP() == null || !paymentToUpdate.getPaymentIdMP().equals(paymentIdMP)) {
                        paymentToUpdate.setPaymentIdMP(paymentIdMP);
                        logger.info("Seteando paymentIdMP {} para el pago ID {}", paymentIdMP, paymentToUpdate.getId());
                    }
                    paymentToUpdate.setPaymentStatus(newStatus);
                    paymentRepository.save(paymentToUpdate);
                    logger.info("Pago ID {} actualizado en la base de datos a estado {}. paymentIdMP: {}",
                            paymentToUpdate.getId(), newStatus, paymentToUpdate.getPaymentIdMP());

                    // Actualiza el estado de la reserva/consultoría si aplica
                    if (paymentToUpdate.getReservation() != null) {
                        reservationService.updateReservationStatusByPayment(paymentToUpdate.getReservation().getId(), String.valueOf(paymentMPDetails.getPaymentStatus()));
                        logger.info("Disparado updateReservationStatusByPayment para Reserva ID {} con estado MP: {}",
                                paymentToUpdate.getReservation().getId(), paymentMPDetails.getPaymentStatus());
                        if (newStatus == PaymentStatus.APPROVED) {
                            Reservation reservation = paymentToUpdate.getReservation();
                            emailService.sendReservationConfirmation(reservation);
                        }
                    } else if (paymentToUpdate.getConsultancy() != null) {
                        consultancyService.updateConsultancyStatusByPayment(paymentToUpdate.getConsultancy().getId(), String.valueOf(paymentMPDetails.getPaymentStatus()));
                        logger.info("Disparado updateConsultancyStatusByPayment para Consultoria ID {} con estado MP: {}",
                                paymentToUpdate.getConsultancy().getId(), paymentMPDetails.getPaymentStatus());
//                        if(newStatus == PaymentStatus.APPROVED) {
//                            Consultancy consultancy = paymentToUpdate.getConsultancy();
//                            ConsultancyResponse consultancyResponse = consultancyService.getConsultancyById(consultancy.getId()).getData();
//                            emailService.sendConsultancyPaidToVeedor(consultancyResponse, emailVeedor);
//                            emailService.sendConsultancyPaidToClient(consultancyResponse);
//                            logger.info("Notificación de pago para consultoría ID {}. Lógica de actualización de consultoría.", paymentToUpdate.getConsultancy().getId());
//                        }
                    }
                } else {
                    logger.info("El pago con ID MP {} ya tiene el estado {}. No se requiere actualización.", paymentIdMP, newStatus);
                }
            } else {
                logger.warn("No se encontró ningún pago PENDING en tu DB para externalId: {} y externalEntityType: {}. No se puede actualizar el pago.",
                        externalIdFromUrl, externalEntityTypeFromUrl);
                // Si no se encontró un PENDING, podrías buscar por el paymentIdMP si ya lo tienes en tu DB
                // o loggear esto como un caso de un webhook de un pago no iniciado por tu sistema.
            }
        } else {
            logger.error("No se pudieron obtener los detalles del pago de Mercado Pago para el ID: {}. Error: {}", paymentIdMP, mpPaymentDetailsResponse.getMessage());
        }
    }
}