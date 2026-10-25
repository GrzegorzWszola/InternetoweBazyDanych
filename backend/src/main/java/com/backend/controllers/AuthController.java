package com.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.services.AuthService;

import com.backend.api.controllers.AuthApi;
import com.backend.api.model.AuthResponse;
import com.backend.api.model.LoginRequest;
import com.backend.api.model.RegisterRequest;
import com.backend.api.model.UserResponse;
import jakarta.validation.Valid;

@RestController 
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<AuthResponse> login(@Valid LoginRequest loginRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public ResponseEntity<UserResponse> register(@Valid RegisterRequest registerRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }
    
}
