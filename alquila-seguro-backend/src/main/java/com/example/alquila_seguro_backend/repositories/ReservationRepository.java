package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByPropertyId(Long propertyId);
    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.property.id = :propertyId AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate) OR " +
            "(r.startDate >= :startDate AND r.startDate <= :endDate)) AND " +
            "r.status = com.example.alquila_seguro_backend.entity.ReservationStatus.CONFIRMED")
    List<Reservation> findOverlappingReservations(Long propertyId, LocalDateTime startDate, LocalDateTime endDate);
}
