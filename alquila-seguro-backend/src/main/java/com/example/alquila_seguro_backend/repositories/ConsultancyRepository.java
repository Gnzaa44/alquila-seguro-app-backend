package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultancyRepository extends JpaRepository<Consultancy, Long> {
    List<Consultancy> findByClientId(Long clientId);
    List<Consultancy> findByPropertyId(Long propertyId);
    List<Consultancy> findByStatus(ConsultancyStatus status);
}
