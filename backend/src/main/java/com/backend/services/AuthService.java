package com.backend.services;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.AuthResponse;
import com.backend.api.model.LoginRequest;
import com.backend.api.model.RegisterRequest;
import com.backend.api.model.UserResponse;
import com.backend.api.model.UserRole;
import com.backend.entities.AppUser;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;

@Service 
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        AppUser user = new AppUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setClearanceLevel(com.backend.entities.ClearanceLevel.BASIC);

        AppUser savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        OffsetDateTime expiresAt = jwtService.getExpirationTimestamp();
        String token = jwtService.generateToken(user, expiresAt.toInstant());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setExpiresAt(expiresAt);
        response.setUser(toUserResponse(user));
        return response;
    }

    public UserResponse toUserResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(UserRole.fromValue(user.getRole().name()));
        response.setClearanceLevel(
            com.backend.api.model.ClearanceLevel.fromValue(user.getClearanceLevel().name())
        );
        return response;
    }
}
