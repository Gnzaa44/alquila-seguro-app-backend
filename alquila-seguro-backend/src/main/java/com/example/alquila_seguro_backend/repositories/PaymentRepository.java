package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    Optional<Payment> findByConsultancyId(Long consultancyId);
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findByExternalId(String externalId);
}
