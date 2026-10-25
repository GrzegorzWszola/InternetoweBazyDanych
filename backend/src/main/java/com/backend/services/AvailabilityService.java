package com.backend.services;

import org.springframework.stereotype.Service;

@Service 
public class AvailabilityService {
    
    private final EquipmentService equipmentService;

    public AvailabilityService(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }
}
