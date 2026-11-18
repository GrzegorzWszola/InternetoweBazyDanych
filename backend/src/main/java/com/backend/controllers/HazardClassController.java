package com.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.HazardClassesApi;
import com.backend.api.model.HazardClassRequest;
import com.backend.api.model.HazardClassResponse;
import com.backend.services.HazardClassService;

import jakarta.validation.Valid;

@RestController
public class HazardClassController implements HazardClassesApi {

    private final HazardClassService hazardClassService;

    public HazardClassController(HazardClassService hazardClassService) {
        this.hazardClassService = hazardClassService;
    }

    @Override
    public ResponseEntity<HazardClassResponse> createHazardClass(@Valid HazardClassRequest hazardClassRequest) {
        HazardClassResponse response = hazardClassService.createHazardClass(hazardClassRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteHazardClass(Long id) {
        hazardClassService.deleteHazardClass(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<HazardClassResponse> getHazardClass(Long id) {
        return ResponseEntity.ok(hazardClassService.getHazardClass(id));
    }

    @Override
    public ResponseEntity<List<HazardClassResponse>> listHazardClasses() {
        return ResponseEntity.ok(hazardClassService.listHazardClasses());
    }

    @Override
    public ResponseEntity<HazardClassResponse> updateHazardClass(Long id, @Valid HazardClassRequest hazardClassRequest) {
        return ResponseEntity.ok(hazardClassService.updateHazardClass(id, hazardClassRequest));
    }
}
