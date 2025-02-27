package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.entity.User;
import com.example.alquila_seguro_backend.repositories.ReservationRepository;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
    @Transactional
    public Payment processPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }
    public List<Reservation> getReservationsByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }



}
