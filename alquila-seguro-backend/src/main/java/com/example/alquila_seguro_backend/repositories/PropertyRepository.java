package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByIsActiveTrue(boolean availability);

}
