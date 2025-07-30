package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.ExternalEntityType;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    Optional<Payment> findByConsultancyId(Long consultancyId);
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findByExternalId(String externalId);
    Optional<Payment> findByPaymentIdMP(String paymentIdMP);
    // Método para buscar un pago PENDING por su Reservation ID
    Optional<Payment> findByReservationIdAndPaymentStatus(Long reservationId, PaymentStatus paymentStatus);

    // Método para buscar un pago PENDING por su Consultancy ID
    Optional<Payment> findByConsultancyIdAndPaymentStatus(Long consultancyId, PaymentStatus paymentStatus);

    Optional<Payment> findFirstByReservationIdAndPaymentStatusOrderByPaymentDateDesc(Long reservationId, PaymentStatus paymentStatus);

    Optional<Payment> findFirstByConsultancyIdAndPaymentStatusOrderByPaymentDateDesc(Long consultancyId, PaymentStatus paymentStatus);

    Optional<Payment> findFirstByExternalIdAndExternalEntityTypeAndPaymentStatusOrderByPaymentDateDesc(String externalId, ExternalEntityType externalEntityType, PaymentStatus paymentStatus);
}
