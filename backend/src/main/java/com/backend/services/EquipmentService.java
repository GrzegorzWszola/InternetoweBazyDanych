package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.EquipmentRepository;

@Service 
public class EquipmentService {
    
    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

}
