package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.AppUserRepository;

@Service 
public class UserService {
    
    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
}
