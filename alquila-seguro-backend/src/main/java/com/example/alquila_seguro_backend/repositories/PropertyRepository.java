package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByPropertyStatus(PropertyStatus propertyStatus);
    List<Property> findByCategory(String category);
    List<Property> findByPricePerNightLessThanEqual(Double maxPrice);


    @Query("SELECT p FROM Property p WHERE p.location LIKE %:location%")
    List<Property> findByLocationContaining(String location);

}
