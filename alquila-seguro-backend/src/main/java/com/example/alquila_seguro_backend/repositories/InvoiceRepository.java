package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.DocumentStatus;
import com.example.alquila_seguro_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByReservationId(Long reservationId);
    List<Invoice> findByStatus(DocumentStatus status);
}
