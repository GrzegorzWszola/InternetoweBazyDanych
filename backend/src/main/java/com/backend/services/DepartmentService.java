package com.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.DepartmentRequest;
import com.backend.api.model.DepartmentResponse;
import com.backend.entities.AppUser;
import com.backend.entities.Department;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.DepartmentRepository;
import com.backend.repositories.EquipmentModelRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EquipmentModelRepository equipmentModelRepository;
    private final AppUserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             EquipmentModelRepository equipmentModelRepository,
                             AppUserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.equipmentModelRepository = equipmentModelRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with id: " + id));
        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        checkAdminRole();

        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name is required");
        }

        if (departmentRepository.existsByName(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department with name '" + request.getName() + "' already exists");
        }

        Department department = new Department();
        department.setName(request.getName().trim());
        department.setLocation(request.getLocation());

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        checkAdminRole();

        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name is required");
        }

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with id: " + id));

        if (departmentRepository.existsByNameAndIdNot(request.getName().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName().trim());
        department.setLocation(request.getLocation());

        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        checkAdminRole();

        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with id: " + id);
        }

        if (equipmentModelRepository.existsByDepartmentId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department still contains equipment models and cannot be deleted");
        }

        departmentRepository.deleteById(id);
    }

    private void checkAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins are allowed to perform this operation");
        }
    }

    public DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse()
                .id(department.getId())
                .name(department.getName())
                .location(department.getLocation());
    }
}
