package com.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.HazardClassRequest;
import com.backend.api.model.HazardClassResponse;
import com.backend.entities.AppUser;
import com.backend.entities.ClearanceLevel;
import com.backend.entities.HazardClass;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.EquipmentModelRepository;
import com.backend.repositories.HazardClassRepository;

@Service
public class HazardClassService {

    private final HazardClassRepository hazardClassRepository;
    private final EquipmentModelRepository equipmentModelRepository;
    private final AppUserRepository userRepository;

    public HazardClassService(HazardClassRepository hazardClassRepository,
                              EquipmentModelRepository equipmentModelRepository,
                              AppUserRepository userRepository) {
        this.hazardClassRepository = hazardClassRepository;
        this.equipmentModelRepository = equipmentModelRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<HazardClassResponse> listHazardClasses() {
        return hazardClassRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HazardClassResponse getHazardClass(Long id) {
        HazardClass hazardClass = hazardClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hazard class not found with id: " + id));
        return mapToResponse(hazardClass);
    }

    @Transactional
    public HazardClassResponse createHazardClass(HazardClassRequest request) {
        checkAdminRole();

        if (request == null || request.getName() == null || request.getName().trim().isEmpty() || request.getRequiredLevel() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hazard class name and required clearance level are required");
        }

        if (hazardClassRepository.existsByName(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hazard class with name '" + request.getName() + "' already exists");
        }

        ClearanceLevel level;
        try {
            level = ClearanceLevel.valueOf(request.getRequiredLevel().getValue());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid clearance level: " + request.getRequiredLevel());
        }

        HazardClass hazardClass = new HazardClass();
        hazardClass.setName(request.getName().trim());
        hazardClass.setRequiredLevel(level);
        hazardClass.setSafetyInstructions(request.getSafetyInstructions());

        HazardClass saved = hazardClassRepository.save(hazardClass);
        return mapToResponse(saved);
    }

    @Transactional
    public HazardClassResponse updateHazardClass(Long id, HazardClassRequest request) {
        checkAdminRole();

        if (request == null || request.getName() == null || request.getName().trim().isEmpty() || request.getRequiredLevel() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hazard class name and required clearance level are required");
        }

        HazardClass hazardClass = hazardClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hazard class not found with id: " + id));

        if (hazardClassRepository.existsByNameAndIdNot(request.getName().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hazard class with name '" + request.getName() + "' already exists");
        }

        ClearanceLevel level;
        try {
            level = ClearanceLevel.valueOf(request.getRequiredLevel().getValue());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid clearance level: " + request.getRequiredLevel());
        }

        hazardClass.setName(request.getName().trim());
        hazardClass.setRequiredLevel(level);
        hazardClass.setSafetyInstructions(request.getSafetyInstructions());

        HazardClass updated = hazardClassRepository.save(hazardClass);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteHazardClass(Long id) {
        checkAdminRole();

        if (!hazardClassRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hazard class not found with id: " + id);
        }

        if (equipmentModelRepository.existsByHazardClassId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hazard class still contains equipment and cannot be deleted");
        }

        hazardClassRepository.deleteById(id);
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

    public HazardClassResponse mapToResponse(HazardClass hazardClass) {
        HazardClassResponse response = new HazardClassResponse();
        response.setId(hazardClass.getId());
        response.setName(hazardClass.getName());
        if (hazardClass.getRequiredLevel() != null) {
            response.setRequiredLevel(com.backend.api.model.ClearanceLevel.fromValue(hazardClass.getRequiredLevel().name()));
        }
        response.setSafetyInstructions(hazardClass.getSafetyInstructions());
        return response;
    }
}
