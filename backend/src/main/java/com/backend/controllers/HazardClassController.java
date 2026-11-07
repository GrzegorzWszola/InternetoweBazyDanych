package com.backend.controllers;

import java.util.List;

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
        throw new UnsupportedOperationException("Unimplemented method 'createHazardClass'");
    }

    @Override
    public ResponseEntity<Void> deleteHazardClass(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteHazardClass'");
    }

    @Override
    public ResponseEntity<HazardClassResponse> getHazardClass(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getHazardClass'");
    }

    @Override
    public ResponseEntity<List<HazardClassResponse>> listHazardClasses() {
        throw new UnsupportedOperationException("Unimplemented method 'listHazardClasses'");
    }

    @Override
    public ResponseEntity<HazardClassResponse> updateHazardClass(Integer id, @Valid HazardClassRequest hazardClassRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'updateHazardClass'");
    }
}
