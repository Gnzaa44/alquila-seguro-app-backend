package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.entity.Booking;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    public List<Payment> getPaymentsByBooking(Booking booking){
        return paymentRepository.findByBooking(booking);
    }
    public List<Payment> getPaymentsByConsultancy(Consultancy consultancy){
        return paymentRepository.findByConsultancy(consultancy);
    }
}
