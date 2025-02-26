package com.example.alquila_seguro_backend.controllers;

import com.example.alquila_seguro_backend.entity.Booking;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/add")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment newPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(newPayment);
    }
    @GetMapping("/booking/{bookingId}")
    public List<Payment> getPaymentsByBooking(@PathVariable Long bookingId) {
        Booking booking = new Booking(bookingId);
        return paymentService.getPaymentsByBooking(booking);
    }
    @GetMapping("/consultancy/{consultancyId}")
    public List<Payment> getPaymentsByConsultancy(@PathVariable Long consultancyId) {
        Consultancy consultancy = new Consultancy(consultancyId);
        return paymentService.getPaymentsByConsultancy(consultancy);
    }
}
