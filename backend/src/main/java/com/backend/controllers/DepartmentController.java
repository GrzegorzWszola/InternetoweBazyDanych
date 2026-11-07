package com.backend.controllers;

import java.util.List;

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
        throw new UnsupportedOperationException("Unimplemented method 'createDepartment'");
    }

    @Override
    public ResponseEntity<Void> deleteDepartment(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteDepartment'");
    }

    @Override
    public ResponseEntity<DepartmentResponse> getDepartment(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getDepartment'");
    }

    @Override
    public ResponseEntity<List<DepartmentResponse>> listDepartments() {
        throw new UnsupportedOperationException("Unimplemented method 'listDepartments'");
    }

    @Override
    public ResponseEntity<DepartmentResponse> updateDepartment(Integer id, @Valid DepartmentRequest departmentRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'updateDepartment'");
    }
}
