package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.AppUserRepository;

@Service 
public class AuthService {

    private final AppUserRepository userRepository;

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
