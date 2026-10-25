package com.backend.controllers;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.services.AvailabilityService;
import com.backend.services.EquipmentService;

import com.backend.api.controllers.EquipmentApi;
import com.backend.api.model.AvailabilityResponse;
import com.backend.api.model.EquipmentPage;
import com.backend.api.model.EquipmentRequest;
import com.backend.api.model.EquipmentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController 
public class EquipmentController implements EquipmentApi{

    private final EquipmentService equipmentService;
    private final AvailabilityService availabilityService;

    public EquipmentController(EquipmentService equipmentService, AvailabilityService availabilityService) {
        this.equipmentService = equipmentService;
        this.availabilityService = availabilityService;
    }

    @Override
    public ResponseEntity<AvailabilityResponse> checkAvailability(Integer id, @NotNull @Valid LocalDate from,
            @NotNull @Valid LocalDate to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkAvailability'");
    }

    @Override
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid EquipmentRequest equipmentRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createEquipment'");
    }

    @Override
    public ResponseEntity<Void> deleteEquipment(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteEquipment'");
    }

    @Override
    public ResponseEntity<EquipmentResponse> getEquipment(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEquipment'");
    }

    @Override
    public ResponseEntity<EquipmentPage> listEquipment(@Valid Integer categoryId, @Valid String q,
            @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listEquipment'");
    }

    @Override
    public ResponseEntity<EquipmentResponse> updateEquipment(Integer id, @Valid EquipmentRequest equipmentRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEquipment'");
    }
    
}
