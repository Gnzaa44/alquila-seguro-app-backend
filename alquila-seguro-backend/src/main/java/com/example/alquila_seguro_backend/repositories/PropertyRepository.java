package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByAvailability(boolean availability);
    List<Property> findByLocation(String location);
}
