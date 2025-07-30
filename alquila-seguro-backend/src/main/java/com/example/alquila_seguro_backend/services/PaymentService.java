package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.mercadopago.MercadoPagoService;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import com.example.alquila_seguro_backend.repositories.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ConsultancyService consultancyService;
    private final ReservationService reservationService;
    private final MercadoPagoService mercadoPagoService;
    private final static Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Transactional
    public Payment createPayment(Long reservationId, Long consultancyId, BigDecimal amount, String paymentMethod, String paymentReference, String paymentDescription) {
        String externalId;
        ExternalEntityType externalEntityType;

        if (reservationId != null) {
            externalId = String.valueOf(reservationId);
            externalEntityType = ExternalEntityType.RESERVATION;
        } else if (consultancyId != null) {
            externalId = String.valueOf(consultancyId);
            externalEntityType = ExternalEntityType.CONSULTANCY;
        } else {
            throw new IllegalArgumentException("Se requiere un reservationId o consultancyId.");
        }

        // --- PASO CLAVE: BUSCAR POR externalId Y externalEntityType ---
        Optional<Payment> existingPendingPayment = paymentRepository.findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(
                externalId, externalEntityType, PaymentStatus.PENDING
        );

        if (existingPendingPayment.isPresent()) {
            Payment payment = existingPendingPayment.get();
            logger.warn("Ya existe un pago PENDING para la entidad {} (type: {}). Reutilizando el pago ID: {}",
                    externalId, externalEntityType, payment.getId());
            // Opcional: Si la preferencia de MP es diferente, puedes actualizar la referencia aquí
             payment.setPaymentReference(paymentReference);
             paymentRepository.save(payment); // Solo si actualizas el existente
            return payment; // Retorna el pago existente
        }


        Payment payment = Payment.builder()
                .reservation(reservationId != null ? Reservation.builder().id(reservationId).build() : null)
                .consultancy(consultancyId != null ? Consultancy.builder().id(consultancyId).build() : null)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentReference(paymentReference)
                .paymentDescription(paymentDescription)
                .paymentDate(java.time.LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .externalId(reservationId != null ? String.valueOf(reservationId) : String.valueOf(consultancyId))
                .externalEntityType(externalEntityType)
                .build();
        try {
            Payment savedPayment = paymentRepository.save(payment);
            logger.info("Pago creado en la base de datos: {}", payment);
            return savedPayment;
        } catch (DataIntegrityViolationException e) {
            // --- MANEJO DE CONDICIÓN DE CARRERA ---
            logger.warn("Condición de carrera detectada: Se intentó crear un pago duplicado para (External ID: {}, Type: {}). Reintentando buscar el pago existente.",
                    externalId, externalEntityType);

            // Volver a intentar buscar el pago PENDING. Ahora debería existir porque el otro hilo ya lo creó.
            Optional<Payment> raceConditionPayment = paymentRepository.findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(
                    externalId, externalEntityType, PaymentStatus.PENDING
            );

            if (raceConditionPayment.isPresent()) {
                // Se encontró el pago que el otro hilo creó. Reutilizarlo.
                logger.info("Recuperado pago existente después de DataIntegrityViolationException: {}", raceConditionPayment.get().getId());
                return raceConditionPayment.get();
            } else {
                // Esto es una situación inusual. Significa que hubo una violación de unicidad
                // pero el pago no se encontró inmediatamente después. Podría indicar un problema
                // con la visibilidad de la transacción o un problema más profundo.
                logger.error("Error crítico: DataIntegrityViolationException pero no se pudo encontrar el pago existente para (External ID: {}, Type: {}).",
                        externalId, externalEntityType, e);
                throw e; // Relanza la excepción si no puedes recuperarte.
            }
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .paymentReference(payment.getPaymentReference())
                .paymentDescription(payment.getPaymentDescription())
                .paymentIdMP(payment.getPaymentIdMP()) // Asegúrate de incluir este campo si lo tienes
                .reservationId(payment.getReservation() != null ? payment.getReservation().getId() : null)
                .consultancyId(payment.getConsultancy() != null ? payment.getConsultancy().getId() : null)
                .externalId(payment.getExternalId())
                .build();
    }


    public Optional<Payment> findPaymentByPaymentReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference);
    }
    public void updatePaymentStatusByPaymentReference(String paymentReference, PaymentStatus newStatus, String paymentIdMP) {
        Optional<Payment> paymentOptional = paymentRepository.findByPaymentReference(paymentReference);
        paymentOptional.ifPresent(payment -> {
            payment.setPaymentStatus(newStatus);
            payment.setPaymentIdMP(paymentIdMP);
            paymentRepository.save(payment);
        });
        if (paymentOptional.isEmpty()) {
            logger.warn("No se encontró el pago con paymentReference: {}", paymentReference);
        }
    }


    @Transactional
    public void updatePaymentIdMPByExternalId(String externalId, String paymentIdMP) {
        // Asegúrate de que `externalId` en tu tabla `payments` sea el `id` de tu reserva/consultoría
        // y que puedas buscarlo por ese campo.
        // Asumiendo que `externalId` en tu entidad Payment es el ID de la reserva o consultoría.
        Optional<Payment> paymentOptional = paymentRepository.findByExternalId(String.valueOf(Long.valueOf(externalId))); // Suponiendo que externalId es un Long en tu entidad

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setPaymentIdMP(paymentIdMP); // ¡Guardar el ID de Mercado Pago!
            paymentRepository.save(payment);
            logger.info("Pago en la base de datos actualizado con paymentIdMP: {} para externalId: {}", paymentIdMP, externalId);
        } else {
            logger.warn("No se encontró el pago en la base de datos con externalId: {}. No se pudo guardar el paymentIdMP.", externalId);
        }
    }

    // ESTE ES EL NUEVO MÉTODO
    @Transactional
    public void updatePaymentIdMPByPaymentReference(String paymentReference, String paymentIdMP) {
        // paymentReference es el ID de la preferencia (preference.getId())
        // paymentIdMP es el ID del pago de Mercado Pago (data.id de la notificación)
        Optional<Payment> paymentOptional = paymentRepository.findByPaymentReference(paymentReference);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setPaymentIdMP(paymentIdMP); // Guarda el ID de Mercado Pago
            paymentRepository.save(payment);
            logger.info("Pago en la base de datos actualizado con paymentIdMP: {} para paymentReference: {}", paymentIdMP, paymentReference);
        } else {
            logger.warn("No se encontró el pago en la base de datos con paymentReference: {}", paymentReference);
        }
    }

    public void updatePaymentIdMPByExternalIdAndType(String externalId, ExternalEntityType externalEntityType, String paymentIdMP) {
        Optional<Payment> paymentOptional = paymentRepository.findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(
                externalId, externalEntityType, PaymentStatus.PENDING
        );

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            if (payment.getPaymentIdMP() == null) {
                payment.setPaymentIdMP(paymentIdMP);
                paymentRepository.save(payment);
                logger.info("Payment ID {} (External ID: {}, Type: {}) actualizado con paymentIdMP: {}",
                        payment.getId(), externalId, externalEntityType, paymentIdMP);
            } else {
                logger.info("Payment ID {} (External ID: {}, Type: {}) ya tiene paymentIdMP. No se actualiza.",
                        payment.getId(), externalId, externalEntityType);
            }
        } else {
            logger.warn("No se encontró un pago PENDING para externalId: {} y externalEntityType: {} para actualizar el paymentIdMP.",
                    externalId, externalEntityType);
        }
    }

    @Transactional
    public void updatePaymentStatusFromMercadoPago(
            String paymentIdMPFromMP, // El ID de pago real de Mercado Pago
            String mpPaymentStatus,   // El estado del pago de Mercado Pago (ej. "approved", "pending")
            String paymentMethod,     // Método de pago de MP
            String paymentReference,  // La referencia de pago de MP (suele ser el ID de la preferencia)
            String externalId,        // Tu ID externo (reservationId o consultancyId)
            ExternalEntityType externalEntityType // Tu tipo de entidad externa
    ) {
        logger.info("updatePaymentStatusFromMercadoPago iniciado para MP Payment ID: {}, MP Status: {}, External ID: {}, Type: {}",
                paymentIdMPFromMP, mpPaymentStatus, externalId, externalEntityType);

        // Mapear el estado de Mercado Pago a tu estado interno
        PaymentStatus newStatus = mercadoPagoService.mapMercadoPagoStatusToYourStatus(mpPaymentStatus);

        // 1. Buscar el pago existente en tu DB. Priorizamos buscar un pago PENDING
        // asociado a la entidad externa para asegurarnos de actualizar el correcto.
        Optional<Payment> paymentOptional = paymentRepository.findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(
                externalId, externalEntityType, PaymentStatus.PENDING);

        Payment paymentToUpdate;

        if (paymentOptional.isPresent()) {
            paymentToUpdate = paymentOptional.get();
            logger.info("Pago PENDING encontrado en tu DB (ID: {}) para External ID: {} y Type: {}",
                    paymentToUpdate.getId(), externalId, externalEntityType);

            // Si el paymentIdMP no está seteado o es diferente, lo actualizamos
            if (paymentToUpdate.getPaymentIdMP() == null || !paymentToUpdate.getPaymentIdMP().equals(paymentIdMPFromMP)) {
                paymentToUpdate.setPaymentIdMP(paymentIdMPFromMP);
                logger.info("Actualizando paymentIdMP del pago {} a {}", paymentToUpdate.getId(), paymentIdMPFromMP);
            }
            // Si la referencia de pago (preference ID) no está seteada o es diferente, la actualizamos
            if (paymentToUpdate.getPaymentReference() == null || !paymentToUpdate.getPaymentReference().equals(paymentReference)) {
                paymentToUpdate.setPaymentReference(paymentReference);
                logger.info("Actualizando paymentReference del pago {} a {}", paymentToUpdate.getId(), paymentReference);
            }
            // Si el método de pago no está seteado, lo actualizamos
            if (paymentToUpdate.getPaymentMethod() == null) {
                paymentToUpdate.setPaymentMethod(paymentMethod);
                logger.info("Actualizando paymentMethod del pago {} a {}", paymentToUpdate.getId(), paymentMethod);
            }

        } else {
            // Si no se encuentra un pago PENDING, intentamos buscar por paymentIdMP.
            // Esto es importante para casos donde el webhook llega tarde y el estado ya fue actualizado
            // por otro webhook, o si el pago ya fue creado con el paymentIdMP.
            logger.warn("No se encontró un pago PENDING para External ID: {} y Type: {}. Intentando buscar por Payment ID MP: {}",
                    externalId, externalEntityType, paymentIdMPFromMP);
            Optional<Payment> existingPaymentByMPId = paymentRepository.findByPaymentIdMP(paymentIdMPFromMP);

            if (existingPaymentByMPId.isPresent()) {
                paymentToUpdate = existingPaymentByMPId.get();
                logger.info("Pago existente encontrado en tu DB por Payment ID MP (ID: {})", paymentToUpdate.getId());
            } else {
                logger.error("No se encontró ningún pago en la base de datos para actualizar (External ID: {}, Type: {}) ni por Payment ID MP: {}. No se puede procesar la actualización.",
                        externalId, externalEntityType, paymentIdMPFromMP);
                return; // No se puede actualizar si no se encuentra el pago
            }
        }

        // Actualizar el estado del pago si ha cambiado
        if (!paymentToUpdate.getPaymentStatus().equals(newStatus)) {
            paymentToUpdate.setPaymentStatus(newStatus);
            paymentRepository.save(paymentToUpdate);
            logger.info("Estado del pago {} actualizado en la base de datos de {} a {}",
                    paymentToUpdate.getId(), paymentToUpdate.getPaymentStatus(), newStatus);

            // Actualizar el estado de la entidad relacionada (Reserva o Consultoría)
            if (externalEntityType == ExternalEntityType.RESERVATION && paymentToUpdate.getReservation() != null) {
                // Asume que reservationService.updateReservationStatusByPayment toma el ID de la reserva y el estado de MP.
                reservationService.updateReservationStatusByPayment(paymentToUpdate.getReservation().getId(), mpPaymentStatus);
                logger.info("Disparado updateReservationStatusByPayment para Reserva ID {} con estado MP: {}",
                        paymentToUpdate.getReservation().getId(), mpPaymentStatus);
            } else if (externalEntityType == ExternalEntityType.CONSULTANCY && paymentToUpdate.getConsultancy() != null) {
                // Aquí deberías llamar a un método similar en tu ConsultancyService
                consultancyService.updateConsultancyStatusByPayment(paymentToUpdate.getConsultancy().getId(), mpPaymentStatus);

                logger.info("Notificación de pago para consultoría ID {}. Lógica de actualización de consultoría (aún no implementada).",
                        paymentToUpdate.getConsultancy().getId());
            }
        } else {
            logger.info("El pago con ID MP {} ya tiene el estado {}. No se requiere actualización de estado.",
                    paymentIdMPFromMP, newStatus);
            // Asegúrate de que el paymentIdMP y la referencia estén guardados aunque el estado no cambie
            if (paymentToUpdate.getPaymentIdMP() == null || !paymentToUpdate.getPaymentIdMP().equals(paymentIdMPFromMP) ||
                    paymentToUpdate.getPaymentReference() == null || !paymentToUpdate.getPaymentReference().equals(paymentReference) ||
                    paymentToUpdate.getPaymentMethod() == null || !paymentToUpdate.getPaymentMethod().equals(paymentMethod)) {
                paymentRepository.save(paymentToUpdate);
                logger.info("Datos de referencia/ID de MP actualizados para pago ID {} aunque el estado no cambió.", paymentToUpdate.getId());
            }
        }
    }

}
