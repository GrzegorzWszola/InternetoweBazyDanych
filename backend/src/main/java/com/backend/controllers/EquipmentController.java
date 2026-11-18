package com.backend.controllers;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.EquipmentApi;
import com.backend.api.model.AvailabilityResponse;
import com.backend.api.model.EquipmentPage;
import com.backend.api.model.EquipmentRequest;
import com.backend.api.model.EquipmentResponse;
import com.backend.services.AvailabilityService;
import com.backend.services.EquipmentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController 
public class EquipmentController implements EquipmentApi {

    private final EquipmentService equipmentService;
    private final AvailabilityService availabilityService;

    public EquipmentController(EquipmentService equipmentService, AvailabilityService availabilityService) {
        this.equipmentService = equipmentService;
        this.availabilityService = availabilityService;
    }

    @Override
    public ResponseEntity<AvailabilityResponse> checkAvailability(Long id, @NotNull @Valid LocalDate from,
            @NotNull @Valid LocalDate to) {
        return ResponseEntity.ok(availabilityService.checkAvailability(id, from, to));
    }

    @Override
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid EquipmentRequest equipmentRequest) {
        EquipmentResponse response = equipmentService.createEquipment(equipmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteEquipment(Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<EquipmentResponse> getEquipment(Long id) {
        return ResponseEntity.ok(equipmentService.getEquipment(id));
    }

    @Override
    public ResponseEntity<EquipmentPage> listEquipment(@Valid Long categoryId, @Valid Long departmentId,
            @Valid Long hazardClassId, @Valid String q, @Min(0) @Valid Integer page,
            @Min(1) @Max(100) @Valid Integer size) {
        return ResponseEntity.ok(equipmentService.listEquipment(categoryId, departmentId, hazardClassId, q, page, size));
    }

    @Override
    public ResponseEntity<EquipmentResponse> updateEquipment(Long id, @Valid EquipmentRequest equipmentRequest) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipmentRequest));
    }
}
