package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClient(Client client);
    List<Reservation> findByStatus(ReservationStatus status);
}
