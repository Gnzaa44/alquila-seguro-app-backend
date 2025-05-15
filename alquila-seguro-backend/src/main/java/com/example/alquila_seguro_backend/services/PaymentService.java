package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.PaymentResponse;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.entity.PaymentStatus;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import com.example.alquila_seguro_backend.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ConsultancyRepository consultancyRepository;
    private final ReservationRepository reservationRepository;
    private final static Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public ApiResponse<PaymentResponse> createPayment(Long reservationId, Long consultancyId, BigDecimal amount, String paymentMethod, String paymentReference, String paymentDescription) {
        logger.info("Entrando a PaymentService.createPayment con reservationId: {}, consultancyId: {}, paymentReference: {}", reservationId, consultancyId, paymentReference);
        Payment payment = null;
        String externalIdToSet = null;
        boolean paymentExists = false;

        if (reservationId != null) {
            externalIdToSet = String.valueOf(reservationId);
            Optional<Payment> existingPayment = paymentRepository.findByExternalId(externalIdToSet);
            if (existingPayment.isPresent()) {
                logger.warn("Ya existe un registro de pago para la reserva con ID: {}. Actualizando referencia.", reservationId);
                payment = existingPayment.get();
                payment.setPaymentReference(paymentReference);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                paymentExists = true;
            } else {
                payment = new Payment();
                Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
                payment.setReservation(reservation);
                payment.setExternalId(externalIdToSet);
                payment.setAmount(amount);
                payment.setPaymentMethod(paymentMethod);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentDescription(paymentDescription);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setPaymentReference(paymentReference);
                payment.setPaymentIdMP(null);
            }
        } else if (consultancyId != null) {
            externalIdToSet = String.valueOf(consultancyId);
            Optional<Payment> existingPayment = paymentRepository.findByExternalId(externalIdToSet);
            if (existingPayment.isPresent()) {
                logger.warn("Ya existe un registro de pago para la consultoría con ID: {}. Actualizando referencia.", consultancyId);
                payment = existingPayment.get();
                payment.setPaymentReference(paymentReference);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                paymentExists = true;
            } else {
                payment = new Payment();
                Consultancy consultancy = consultancyRepository.findById(consultancyId).orElse(null);
                payment.setConsultancy(consultancy);
                payment.setExternalId(externalIdToSet);
                payment.setAmount(amount);
                payment.setPaymentMethod(paymentMethod);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentDescription(paymentDescription);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setPaymentReference(paymentReference);
                payment.setPaymentIdMP(null);
            }
        }

        if (payment == null) {
            return ApiResponse.<PaymentResponse>builder().success(false).message("No se especificó reserva o consultoría.").build();
        }

        payment = paymentRepository.save(payment); // Guardar o actualizar el registro
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message(paymentExists ? "Registro de pago actualizado." : "Registro de pago creado.")
                .data(mapToPaymentResponse(payment))
                .build();
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



}
