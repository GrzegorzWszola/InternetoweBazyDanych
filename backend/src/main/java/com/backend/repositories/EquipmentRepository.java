package com.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.entities.Equipment;

@Repository 
public interface EquipmentRepository extends JpaRepository<Equipment, Integer>{
    
}
