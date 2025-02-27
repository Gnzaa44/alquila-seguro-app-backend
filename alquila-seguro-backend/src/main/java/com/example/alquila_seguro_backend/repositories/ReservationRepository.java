package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByStatus(String status);
}
