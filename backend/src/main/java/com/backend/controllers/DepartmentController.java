package com.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.DepartmentsApi;
import com.backend.api.model.DepartmentRequest;
import com.backend.api.model.DepartmentResponse;
import com.backend.services.DepartmentService;

import jakarta.validation.Valid;

@RestController
public class DepartmentController implements DepartmentsApi {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid DepartmentRequest departmentRequest) {
        DepartmentResponse response = departmentService.createDepartment(departmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteDepartment(Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<DepartmentResponse> getDepartment(Long id) {
        return ResponseEntity.ok(departmentService.getDepartment(id));
    }

    @Override
    public ResponseEntity<List<DepartmentResponse>> listDepartments() {
        return ResponseEntity.ok(departmentService.listDepartments());
    }

    @Override
    public ResponseEntity<DepartmentResponse> updateDepartment(Long id, @Valid DepartmentRequest departmentRequest) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentRequest));
    }
}
