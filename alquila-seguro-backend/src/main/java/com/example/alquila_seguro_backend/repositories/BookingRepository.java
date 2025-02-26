package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Booking;
import com.example.alquila_seguro_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByStatus(String status);
}
