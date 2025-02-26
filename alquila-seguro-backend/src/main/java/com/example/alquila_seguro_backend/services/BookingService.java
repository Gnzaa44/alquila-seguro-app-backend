package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.entity.Booking;
import com.example.alquila_seguro_backend.entity.Payment;
import com.example.alquila_seguro_backend.entity.User;
import com.example.alquila_seguro_backend.repositories.BookingRepository;
import com.example.alquila_seguro_backend.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Transactional
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    @Transactional
    public Payment processPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUser(user);
    }
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }



}
