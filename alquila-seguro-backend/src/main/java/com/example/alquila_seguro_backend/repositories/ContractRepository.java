package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Contract;
import com.example.alquila_seguro_backend.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByReservationId(Long reservationId);
    List<Contract> findByStatus(DocumentStatus status);
}
