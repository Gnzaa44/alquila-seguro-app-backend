package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Contract findByReservationId(Long reservationId);
}
