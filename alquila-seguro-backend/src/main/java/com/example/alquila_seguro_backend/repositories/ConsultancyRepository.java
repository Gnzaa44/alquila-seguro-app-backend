package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConsultancyRepository extends JpaRepository<Consultancy, Long> {
    List<Consultancy> findByUser(User user);
}
