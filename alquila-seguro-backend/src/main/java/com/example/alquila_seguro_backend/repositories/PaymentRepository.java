package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Booking;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List <Payment> findByBooking(Booking booking);
    List <Payment> findByConsultancy(Consultancy consultancy);

}
