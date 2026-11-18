package com.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.ChangeClearanceRequest;
import com.backend.api.model.ChangeRoleRequest;
import com.backend.api.model.UserPage;
import com.backend.api.model.UserResponse;
import com.backend.api.model.UserRole;
import com.backend.entities.AppUser;
import com.backend.entities.ClearanceLevel;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;

@Service 
public class UserService {
    
    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserPage listUsers(Integer page, Integer size) {
        int pageIndex = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 20;

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<AppUser> userPage = userRepository.findAll(pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new UserPage()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse changeUserRole(Long id, ChangeRoleRequest request) {
        checkAdminRole();
        if (request == null || request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role cannot be null");
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        Role newRole;
        try {
            newRole = Role.valueOf(request.getRole().getValue());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + request.getRole());
        }

        user.setRole(newRole);
        AppUser updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    @Transactional
    public UserResponse changeUserClearance(Long id, ChangeClearanceRequest request) {
        checkAdminRole();
        if (request == null || request.getClearanceLevel() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clearance level cannot be null");
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        ClearanceLevel newClearance;
        try {
            newClearance = ClearanceLevel.valueOf(request.getClearanceLevel().getValue());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid clearance level: " + request.getClearanceLevel());
        }

        user.setClearanceLevel(newClearance);
        AppUser updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    public UserResponse mapToResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        if (user.getRole() != null) {
            response.setRole(UserRole.fromValue(user.getRole().name()));
        }
        if (user.getClearanceLevel() != null) {
            response.setClearanceLevel(
                com.backend.api.model.ClearanceLevel.fromValue(user.getClearanceLevel().name())
            );
        }
        return response;
    }

    private void checkAdminRole() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins are allowed to perform this operation");
        }
    }
}
