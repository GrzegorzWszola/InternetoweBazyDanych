package com.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.entities.EquipmentAttribute;

@Repository
public interface EquipmentAttributeRepository extends JpaRepository<EquipmentAttribute, Integer> {
    List<EquipmentAttribute> findByEquipmentId(Integer equipmentId);
}
