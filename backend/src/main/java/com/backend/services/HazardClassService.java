package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.HazardClassRepository;

@Service
public class HazardClassService {

    private final HazardClassRepository hazardClassRepository;

    public HazardClassService(HazardClassRepository hazardClassRepository) {
        this.hazardClassRepository = hazardClassRepository;
    }
}
