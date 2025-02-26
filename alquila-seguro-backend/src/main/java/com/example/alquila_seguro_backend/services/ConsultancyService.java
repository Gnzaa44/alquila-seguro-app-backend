package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.User;
import com.example.alquila_seguro_backend.repositories.ConsultancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultancyService {
    @Autowired
    private ConsultancyRepository consultancyRepository;

    public Consultancy createConsultancy(Consultancy consultancy) {
        return consultancyRepository.save(consultancy);
    }
    public List<Consultancy> getAllConsultancy() {
        return consultancyRepository.findAll();
    }
    public List<Consultancy> getConsultancyByUser(User user) {
        return consultancyRepository.findByUser(user);
    }
}
